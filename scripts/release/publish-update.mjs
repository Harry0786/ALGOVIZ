import { randomUUID } from "node:crypto";
import { readFile } from "node:fs/promises";
import process from "node:process";

import admin from "firebase-admin";

function required(name) {
  const value = process.env[name];
  if (!value || value.trim() === "") {
    throw new Error(`Missing required environment variable: ${name}`);
  }
  return value;
}

function optional(name) {
  const value = process.env[name];
  if (!value || value.trim() === "") {
    return null;
  }
  return value;
}

async function publishToSupabase({
  supabaseUrl,
  supabaseServiceRoleKey,
  versionCode,
  versionName,
  apkUrl,
  releaseNotes,
  forceUpdate
}) {
  if (!supabaseUrl || !supabaseServiceRoleKey) {
    return false;
  }

  const endpoint = `${supabaseUrl.replace(/\/$/, "")}/rest/v1/app_config?on_conflict=id`;
  const payload = [
    {
      id: "latest_version",
      version_code: versionCode,
      version_name: versionName,
      apk_url: apkUrl,
      release_notes: releaseNotes,
      force_update: forceUpdate,
      updated_at: Date.now()
    }
  ];

  const response = await fetch(endpoint, {
    method: "POST",
    headers: {
      apikey: supabaseServiceRoleKey,
      Authorization: `Bearer ${supabaseServiceRoleKey}`,
      "Content-Type": "application/json",
      Prefer: "resolution=merge-duplicates,return=minimal"
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`Supabase publish failed (${response.status}): ${body}`);
  }

  return true;
}

async function main() {
  const serviceAccountRaw = optional("FIREBASE_SERVICE_ACCOUNT_JSON");
  const projectId = optional("FIREBASE_PROJECT_ID");
  const configuredBucketName = optional("FIREBASE_STORAGE_BUCKET");
  const googleServicesJsonBase64 = optional("GOOGLE_SERVICES_JSON_BASE64");
  const apkPath = optional("APK_PATH");
  const apkPublicUrlFromEnv = optional("APK_PUBLIC_URL");
  const versionCode = Number(required("VERSION_CODE"));
  const versionName = required("VERSION_NAME");
  const releaseNotes = process.env.RELEASE_NOTES?.trim() || `Automated release ${versionName}`;
  const forceUpdate = String(process.env.FORCE_UPDATE || "false").toLowerCase() === "true";
  const supabaseUrl = optional("SUPABASE_URL");
  const supabaseServiceRoleKey = optional("SUPABASE_SERVICE_ROLE_KEY");

  if (!Number.isInteger(versionCode) || versionCode <= 0) {
    throw new Error("VERSION_CODE must be a positive integer");
  }

  const objectPath = `releases/algoviz-v${versionName}-${versionCode}.apk`;
  const downloadToken = randomUUID();

  let apkUrl = apkPublicUrlFromEnv;

  if (!apkUrl) {
    if (!serviceAccountRaw || !projectId) {
      throw new Error("APK_PUBLIC_URL is required when Firebase upload credentials are not configured");
    }

    const serviceAccount = JSON.parse(serviceAccountRaw);
    if (!admin.apps.length) {
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        projectId
      });
    }

    if (!apkPath) {
      throw new Error("APK_PUBLIC_URL or APK_PATH must be provided");
    }

    const apkBytes = await readFile(apkPath);

    // Fallback upload path via Firebase Storage.
    let googleServicesBucket = null;
    if (googleServicesJsonBase64) {
      try {
        const googleServicesJson = Buffer.from(googleServicesJsonBase64, "base64").toString("utf8");
        const parsed = JSON.parse(googleServicesJson);
        googleServicesBucket = parsed?.project_info?.storage_bucket || null;
      } catch {
        // Ignore parse failures; other candidates still apply.
      }
    }

    const bucketCandidates = Array.from(new Set([
      configuredBucketName,
      googleServicesBucket,
      `${projectId}.appspot.com`,
      `${projectId}.firebasestorage.app`
    ].filter(Boolean)));

    let bucket = null;
    let bucketName = null;
    for (const candidate of bucketCandidates) {
      try {
        const current = admin.storage().bucket(candidate);
        const [exists] = await current.exists();
        if (exists) {
          bucket = current;
          bucketName = candidate;
          break;
        }
      } catch {
        // Keep trying the next candidate.
      }
    }

    if (!bucket || !bucketName) {
      throw new Error(
        `No valid storage bucket found. Tried: ${bucketCandidates.join(", ")}. ` +
        "Provide APK_PUBLIC_URL (preferred) or fix FIREBASE_STORAGE_BUCKET."
      );
    }

    console.log(`Using storage bucket: ${bucketName}`);

    const file = bucket.file(objectPath);
    await file.save(apkBytes, {
      resumable: false,
      contentType: "application/vnd.android.package-archive",
      metadata: {
        metadata: {
          firebaseStorageDownloadTokens: downloadToken
        },
        cacheControl: "public, max-age=3600"
      }
    });

    const encodedObjectPath = encodeURIComponent(objectPath);
    apkUrl = `https://firebasestorage.googleapis.com/v0/b/${bucketName}/o/${encodedObjectPath}?alt=media&token=${downloadToken}`;
  }

  let publishedToFirebase = false;
  if (serviceAccountRaw && projectId) {
    try {
      const serviceAccount = JSON.parse(serviceAccountRaw);
      if (!admin.apps.length) {
        admin.initializeApp({
          credential: admin.credential.cert(serviceAccount),
          projectId
        });
      }

      await admin
        .firestore()
        .collection("app_config")
        .doc("latest_version")
        .set({
          versionCode,
          versionName,
          apkUrl,
          releaseNotes,
          forceUpdate,
          updatedAt: Date.now(),
          source: "github-actions"
        }, { merge: true });
      publishedToFirebase = true;
    } catch (error) {
      console.warn(`Firebase metadata publish skipped: ${error?.message || error}`);
    }
  }

  const publishedToSupabase = await publishToSupabase({
    supabaseUrl,
    supabaseServiceRoleKey,
    versionCode,
    versionName,
    apkUrl,
    releaseNotes,
    forceUpdate
  });

  console.log("Published update metadata successfully.");
  console.log(`APK URL: ${apkUrl}`);
  console.log(`Firebase metadata published: ${publishedToFirebase}`);
  console.log(`Supabase metadata published: ${publishedToSupabase}`);

  if (!publishedToFirebase && !publishedToSupabase) {
    throw new Error("No update metadata destination succeeded (Firebase and Supabase both unavailable)");
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
