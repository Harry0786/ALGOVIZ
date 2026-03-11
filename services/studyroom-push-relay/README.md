# Study Room Push Relay (No Blaze)

This service replaces Firebase Cloud Functions for Study Room push notifications.
It listens to Firestore `messages` documents and sends FCM topic notifications.

## Why this exists

Cloud Functions Gen2 requires Blaze billing. This relay runs on any Node host (Render, Railway, VPS) and works with Firebase Spark.

## What it does

- Watches Firestore collection-group: `messages`
- For each new message:
  - Skips SYSTEM messages
  - Sends push to topic `study_room_<roomId>`
  - Writes dedupe marker in `push_processed_messages/{roomId_messageId}`

## Required environment variables

- `FIREBASE_PROJECT_ID` (example: `algoviz-plus`)
- One of:
  - `FIREBASE_SERVICE_ACCOUNT_JSON`
  - `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64`

## Local run

```bash
cd services/studyroom-push-relay
npm install
npm start
```

## Deploy on Render (background worker)

- Create a Render **Background Worker**.
- Connect this GitHub repo.
- Set Root Directory: `services/studyroom-push-relay`
- Build Command: `npm install`
- Start Command: `npm start`
- Add env vars:
  - `FIREBASE_PROJECT_ID=algoviz-plus`
  - `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64=<base64 of algoviz-plus-firebase-adminsdk-fbsvc-beec5d2ea0.json>`

Windows command to generate base64 (use Admin SDK file, not `google-services.json`):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\Users\jayes\OneDrive\Desktop\ALGOVIZ\algoviz-plus-firebase-adminsdk-fbsvc-beec5d2ea0.json"))
```

After deploy, verify logs in Render should show:

```text
[relay] Started for project=algoviz-plus
```

## Deploy on Railway


## Notes

- Keep your app topic naming unchanged (`study_room_<roomId>`).
- App is already subscribing/unsubscribing to room topics.
- This service must stay running continuously.
- Never commit credential files or base64 values.


## 5) One-command VPS setup (recommended)

From your VPS shell, run the generated setup script in this repo:

```bash
chmod +x scripts/push-relay/setup-vps.sh scripts/push-relay/update-vps.sh
./scripts/push-relay/setup-vps.sh "<FIREBASE_SERVICE_ACCOUNT_JSON_BASE64>" algoviz-plus "https://github.com/Harry0786/ALGOVIZ.git"
```

After code changes, update and restart with:

```bash
./scripts/push-relay/update-vps.sh
```

Useful PM2 commands:

```bash
pm2 status
pm2 logs studyroom-push-relay
pm2 restart studyroom-push-relay --update-env
```

## 6) Deploy from Windows over SSH

If you are on Windows, use the helper script from the repo root:

```powershell
./scripts/push-relay/deploy-vps.ps1 -Host <VPS_IP_OR_DOMAIN> -User <SSH_USER>
```

With an SSH key:

```powershell
./scripts/push-relay/deploy-vps.ps1 -Host <VPS_IP_OR_DOMAIN> -User <SSH_USER> -SshKeyPath "C:\path\to\key.pem"
```
