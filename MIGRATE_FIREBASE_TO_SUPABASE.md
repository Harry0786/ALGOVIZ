# Firebase to Supabase data migration

This repo now has a one-time migration utility to move existing Firestore data to Supabase.

## What gets migrated

- `study_rooms` -> `study_rooms`
- `study_rooms/{roomId}/members` -> `study_room_members`
- `study_rooms/{roomId}/messages` -> `study_room_messages`
- `user_presence` -> `user_presence`
- `app_config/latest_version` -> `app_config`
- `user_profiles` -> `user_profiles`

## 1) Create Supabase tables

Run SQL from:

- `scripts/supabase_studyroom_schema.sql`

in your Supabase SQL editor.

## 2) Set keys (required for apply)

In `local.properties` set:

- `SUPABASE_URL=https://<project-ref>.supabase.co`
- `SUPABASE_SERVICE_ROLE_KEY=<service-role-key>`

`SUPABASE_KEY` can be used if service role key is not present, but service role key is strongly recommended for full migration.

## 3) Install migration dependency

```powershell
pip install firebase-admin
```

## 4) Dry run (no Supabase write)

```powershell
python scripts/migrate_firebase_to_supabase.py
```

This validates Firebase extraction and prints planned row counts. Supabase keys are not required for this step.

## 5) Apply migration (writes to Supabase)

```powershell
python scripts/migrate_firebase_to_supabase.py --apply
```

## 6) Verify migrated data

Check row counts in Supabase tables:

- `study_rooms`
- `study_room_members`
- `study_room_messages`
- `user_presence`
- `app_config`
- `user_profiles`

## Notes

- The app runtime still has Firebase-backed study-room operations in `data/.../FirebaseStudyRoomDataSource.kt`.
- This data migration script safely moves existing Firestore data now; runtime replacement to Supabase Realtime/PostgREST can be completed in the next step without losing historical data.
