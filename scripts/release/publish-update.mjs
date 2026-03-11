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

async function main() {
  const serviceAccountRaw = required("FIREBASE_SERVICE_ACCOUNT_JSON");
  const projectId = required("FIREBASE_PROJECT_ID");
  const configuredBucketName = required("FIREBASE_STORAGE_BUCKET");
  const apkPath = required("APK_PATH");
  const versionCode = Number(required("VERSION_CODE"));
  const versionName = required("VERSION_NAME");
  const releaseNotes = process.env.RELEASE_NOTES?.trim() || `Automated release ${versionName}`;
  const forceUpdate = String(process.env.FORCE_UPDATE || "false").toLowerCase() === "true";

  if (!Number.isInteger(versionCode) || versionCode <= 0) {
    throw new Error("VERSION_CODE must be a positive integer");
  }

  const serviceAccount = JSON.parse(serviceAccountRaw);
  const apkBytes = await readFile(apkPath);
  const objectPath = `releases/algoviz-v${versionName}-${versionCode}.apk`;
  const downloadToken = randomUUID();

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId
  });

  // Different Firebase/GCS setups may expose either .appspot.com or .firebasestorage.app.
  const bucketCandidates = Array.from(new Set([
    configuredBucketName,
    `${projectId}.appspot.com`,
    `${projectId}.firebasestorage.app`
  ]));

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
      "Update FIREBASE_STORAGE_BUCKET secret to your real bucket name."
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
  const apkUrl = `https://firebasestorage.googleapis.com/v0/b/${bucketName}/o/${encodedObjectPath}?alt=media&token=${downloadToken}`;

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
