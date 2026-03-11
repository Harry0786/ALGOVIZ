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

async function main() {
  const serviceAccountRaw = required("FIREBASE_SERVICE_ACCOUNT_JSON");
  const projectId = required("FIREBASE_PROJECT_ID");
  const configuredBucketName = optional("FIREBASE_STORAGE_BUCKET");
  const googleServicesJsonBase64 = optional("GOOGLE_SERVICES_JSON_BASE64");
  const apkPath = optional("APK_PATH");
  const apkPublicUrlFromEnv = optional("APK_PUBLIC_URL");
  const versionCode = Number(required("VERSION_CODE"));
  const versionName = required("VERSION_NAME");
  const releaseNotes = process.env.RELEASE_NOTES?.trim() || `Automated release ${versionName}`;
  const forceUpdate = String(process.env.FORCE_UPDATE || "false").toLowerCase() === "true";

  if (!Number.isInteger(versionCode) || versionCode <= 0) {
    throw new Error("VERSION_CODE must be a positive integer");
  }

  const serviceAccount = JSON.parse(serviceAccountRaw);
  const objectPath = `releases/algoviz-v${versionName}-${versionCode}.apk`;
  const downloadToken = randomUUID();

  let apkUrl = apkPublicUrlFromEnv;

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId
  });

  if (!apkUrl) {
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

  console.log("Published update metadata successfully.");
  console.log(`APK URL: ${apkUrl}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
