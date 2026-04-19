# Study Rooms Fix - Testing & Verification Guide

## ✅ Testing Steps

### 1. **Compile Check** (Already Done)
```bash
.\gradlew.bat :app:compileDebugKotlin --no-daemon
# Result: BUILD SUCCESSFUL ✓
```

### 2. **Run Full Build**
```bash
.\gradlew.bat :app:assembleDebug --no-daemon
# Look for: BUILD SUCCESSFUL
```

### 3. **Run on Device/Emulator**
- Install debug APK on test device
- Clear app data: Settings → Apps → AlgoViz → Storage → Clear All Data
- Force close and reopen the app

### 4. **Manual Test Cases**

#### Test Case 1: Avatar Display
1. Open Study Rooms screen
2. Look at room member preview avatars (top-right of room cards)
3. **Expected**: Members show circular avatar images (not just initials)
4. **Issue Fixed**: Avatar data now properly fetched from user_profiles

#### Test Case 2: Member Name Consistency
1. Create or join a study room
2. Compare member names across:
   - Room member list in Study Rooms
   - Chat room member sidebar
   - Online friends section
3. **Expected**: Same names display everywhere
4. **Issue Fixed**: toDto() now properly maps avatarUrl, display names normalized

#### Test Case 3: Member Information Display
1. Click on a room to view full member list
2. Check each member shows:
   - Avatar image
   - Display name
   - Online status indicator
3. **Expected**: All fields populate correctly
4. **Issue Fixed**: RoomMemberRow.toDto() now includes avatarUrl parameter

#### Test Case 4: Room Sync
1. Have User A create a study room
2. Have User B join the same room
3. User A should see User B in member list with avatar
4. **Expected**: Avatar appears immediately or within 2-3 seconds
5. **Issue Fixed**: observeRoomMembers() properly joins user_profiles for avatars

### 5. **Database Verification** (Optional but Recommended)

Run these queries in Supabase SQL editor to verify data integrity:

#### Query 1: Check member profile coverage
```sql
SELECT
    COUNT(*) as total_members,
    COUNT(DISTINCT CASE WHEN p.user_id IS NOT NULL THEN m.user_id END) as members_with_profiles,
    ROUND(100.0 * COUNT(DISTINCT CASE WHEN p.user_id IS NOT NULL THEN m.user_id END) / COUNT(*))::text || '%' as coverage
FROM public.study_room_members m
LEFT JOIN public.user_profiles p ON p.user_id = m.user_id
WHERE EXISTS (SELECT 1 FROM public.study_rooms r WHERE r.id = m.room_id AND r.is_active = true);
-- Expected: Coverage should be 100%
```

#### Query 2: Check avatar distribution
```sql
SELECT
    COUNT(DISTINCT CASE WHEN p.avatar_url IS NOT NULL THEN p.user_id END) as profiles_with_avatars,
    COUNT(DISTINCT p.user_id) as total_profiles,
    ROUND(100.0 * COUNT(DISTINCT CASE WHEN p.avatar_url IS NOT NULL THEN p.user_id END) / COUNT(DISTINCT p.user_id))::text || '%' as avatar_coverage
FROM public.study_room_members m
LEFT JOIN public.user_profiles p ON p.user_id = m.user_id;
-- Expected: Coverage should be high (depends on users who have uploaded avatars)
```

#### Query 3: Check member name consistency
```sql
SELECT
    m.user_id,
    m.user_name as member_stored_name,
    p.name as profile_name,
    CASE WHEN m.user_name != p.name THEN 'MISMATCH' ELSE 'OK' END as status
FROM public.study_room_members m
LEFT JOIN public.user_profiles p ON p.user_id = m.user_id
LIMIT 20;
-- Expected: Most should show OK
```

#### Query 4: RLS Policy Test (As Authenticated User)
```sql
-- This tests if RLS policies allow reading shared room member profiles
SELECT
    p.user_id,
    p.name,
    p.avatar_url,
    COUNT(DISTINCT m.room_id) as shared_rooms
FROM public.user_profiles p
JOIN public.study_room_members m_them ON m_them.user_id = p.user_id
JOIN public.study_room_members m_me ON m_me.room_id = m_them.room_id
WHERE m_me.user_id = auth.uid()::text
GROUP BY p.user_id, p.name, p.avatar_url
LIMIT 10;
-- Expected: Should return list of profiles (if user is in any study room)
```

## 🔧 Debugging Steps (If Issues Persist)

### If Avatars Still Don't Show:

1. **Check Supabase Storage**:
   - Go to Supabase Dashboard → Storage → Algoviz
   - Verify avatar files exist in profile_images folder
   - Check file permissions (should be public read)

2. **Check RLS Policies**:
   - Run the RLS test query above
   - If no results, policies might be blocking access
   - Check user is in study_room_members table

3. **Check User Profile Data**:
   - Query: `SELECT user_id, name, username, avatar_url FROM user_profiles LIMIT 10;`
   - Ensure avatar_url field is populated
   - Check URL format (should start with https:// or /storage/)

4. **Check App Logs**:
   - Look for errors in logcat related to avatar fetching
   - Check network calls in Network tab

### If Member Names Are Wrong:

1. **Check study_room_members table**:
   - Query: `SELECT user_id, user_name FROM study_room_members LIMIT 10;`
   - user_name should match profile names, not UUIDs

2. **Run data fix migration**:
   - Execute: `scripts/fix_study_rooms_member_data.sql`
   - This syncs member names from user_profiles

3. **Check UserIdentityUtils**:
   - Verify display name resolution logic
   - Check PreferencesManager cache values

## 📊 Performance Considerations

- Avatar URLs are fetched once per polling cycle (1200ms default)
- Profile lookups use indexed queries (idx_user_profiles_user_id)
- Members are sorted by username for stable ordering

## 🎯 Success Criteria

✅ All study room members display avatars
✅ Member names are consistent across screens
✅ Online/offline status updates correctly
✅ No crashes when loading rooms
✅ No RLS permission errors in logs

## 📋 Checklist

- [ ] Recompiled app with changes
- [ ] Tested avatar display in Study Rooms
- [ ] Verified member names are consistent
- [ ] Checked no crashes when joining rooms
- [ ] Verified online status indicators work
- [ ] (Optional) Ran database diagnostic queries
- [ ] (Optional) Ran data fix migration if inconsistencies found
