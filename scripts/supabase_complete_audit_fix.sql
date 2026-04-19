-- ============================================================
-- COMPLETE SUPABASE AUDIT & FIX SCRIPT
-- ============================================================
-- Execute this script directly in Supabase SQL Editor
-- It will identify and fix all issues in one go
-- ============================================================

-- ============================================================
-- PART 1: PRE-FIX DIAGNOSTICS
-- ============================================================
-- Run these queries FIRST to see current issues

SELECT '========== PRE-FIX DIAGNOSTICS ==========' as status;

-- Diagnostic 1: Count issues
SELECT 
    'Auth users without profiles' as issue_type,
    COUNT(*) as count
FROM auth.users u
LEFT JOIN public.user_profiles p ON p.user_id = u.id::text
WHERE p.user_id IS NULL
UNION ALL
SELECT 'Study room members without profiles' as issue_type,
    COUNT(*) as count
FROM public.study_room_members m
LEFT JOIN public.user_profiles p ON p.user_id = m.user_id
WHERE p.user_id IS NULL AND m.user_id != ''
UNION ALL
SELECT 'Members with empty names' as issue_type,
    COUNT(*) as count
FROM public.study_room_members
WHERE user_name = '' OR user_name IS NULL
UNION ALL
SELECT 'Profiles with empty names' as issue_type,
    COUNT(*) as count
FROM public.user_profiles
WHERE name = '' OR name IS NULL
UNION ALL
SELECT 'Rooms with incorrect member count' as issue_type,
    COUNT(*) as count
FROM public.study_rooms r
WHERE r.member_count != (
    SELECT COUNT(*) FROM public.study_room_members WHERE room_id = r.id
);

-- ============================================================
-- PART 2: FIX ALL IDENTIFIED ISSUES
-- ============================================================

BEGIN;

-- FIX 1: Create missing user_profiles for orphaned members
INSERT INTO public.user_profiles (
    user_id,
    name,
    username,
    email,
    phone_no,
    avatar_url,
    avatar_color_index,
    updated_at
)
SELECT DISTINCT
    m.user_id,
    COALESCE(m.user_name, 'AlgoViz User'),
    COALESCE(NULLIF(LOWER(REGEXP_REPLACE(m.user_name, ' +', '_')), ''), 'user'),
    'user_' || SUBSTR(m.user_id, 1, 8) || '@algoviz.local',
    '',
    NULL,
    0,
    EXTRACT(EPOCH FROM NOW())::BIGINT
FROM public.study_room_members m
LEFT JOIN public.user_profiles p ON p.user_id = m.user_id
WHERE p.user_id IS NULL
  AND m.user_id != ''
ON CONFLICT (user_id) DO NOTHING;

-- FIX 2: Sync auth.users without profiles
INSERT INTO public.user_profiles (
    user_id,
    name,
    username,
    email,
    phone_no,
    avatar_url,
    avatar_color_index,
    updated_at
)
SELECT
    u.id::TEXT as user_id,
    COALESCE(
        NULLIF(u.raw_user_meta_data ->> 'name', ''),
        NULLIF(u.raw_user_meta_data ->> 'full_name', ''),
        NULLIF(u.raw_user_meta_data ->> 'user_name', ''),
        NULLIF(u.raw_user_meta_data ->> 'preferred_username', ''),
        SPLIT_PART(COALESCE(u.email, ''), '@', 1),
        'AlgoViz User'
    ) as name,
    COALESCE(
        NULLIF(u.raw_user_meta_data ->> 'username', ''),
        NULLIF(u.raw_user_meta_data ->> 'user_name', ''),
        NULLIF(u.raw_user_meta_data ->> 'preferred_username', ''),
        LOWER(SPLIT_PART(COALESCE(u.email, ''), '@', 1)),
        'user'
    ) as username,
    COALESCE(u.email, '') as email,
    COALESCE(
        NULLIF(u.raw_user_meta_data ->> 'phoneNumber', ''),
        NULLIF(u.raw_user_meta_data ->> 'phone', ''),
        COALESCE(u.phone, ''),
        ''
    ) as phone_no,
    NULLIF(COALESCE(u.raw_user_meta_data ->> 'avatarUrl', ''), '') as avatar_url,
    COALESCE((u.raw_user_meta_data ->> 'avatarColorIndex')::INTEGER, 0) as avatar_color_index,
    EXTRACT(EPOCH FROM NOW())::BIGINT as updated_at
FROM auth.users u
LEFT JOIN public.user_profiles p ON p.user_id = u.id::TEXT
WHERE p.user_id IS NULL
ON CONFLICT (user_id) DO UPDATE
SET
    email = EXCLUDED.email,
    updated_at = EXCLUDED.updated_at;

-- FIX 3: Update study_room_members with correct names from profiles
UPDATE public.study_room_members m
SET user_name = COALESCE(
    NULLIF(p.name, ''),
    NULLIF(p.username, ''),
    SPLIT_PART(COALESCE(p.email, ''), '@', 1),
    m.user_name
)
FROM public.user_profiles p
WHERE m.user_id = p.user_id
  AND (m.user_name = '' OR m.user_name = m.user_id OR m.user_name IS NULL)
  AND p.name != '';

-- FIX 4: Fix empty profile names
UPDATE public.user_profiles
SET 
    name = COALESCE(
        NULLIF(username, ''),
        SPLIT_PART(COALESCE(email, ''), '@', 1),
        'AlgoViz User'
    ),
    username = COALESCE(
        NULLIF(username, ''),
        LOWER(SPLIT_PART(COALESCE(email, ''), '@', 1)),
        'user'
    )
WHERE name = '' OR name IS NULL;

-- FIX 5: Fix empty emails
UPDATE public.user_profiles
SET email = 'user_' || SUBSTR(user_id, 1, 8) || '@algoviz.local'
WHERE email = '' OR email IS NULL;

-- FIX 6: Resync all study_room member counts
UPDATE public.study_rooms r
SET member_count = (
    SELECT COUNT(*) FROM public.study_room_members WHERE room_id = r.id
)
WHERE is_active = true;

-- FIX 7: Clean up invalid presence entries
DELETE FROM public.user_presence
WHERE user_id NOT IN (
    SELECT DISTINCT user_id FROM public.study_room_members
    UNION
    SELECT id::TEXT FROM auth.users
)
OR user_id = '' OR user_id IS NULL;

-- FIX 8: Add missing presence entries for active members
INSERT INTO public.user_presence (user_id, is_online, last_seen_at)
SELECT DISTINCT
    m.user_id,
    m.is_online,
    COALESCE(m.last_seen_at, EXTRACT(EPOCH FROM NOW())::BIGINT)
FROM public.study_room_members m
LEFT JOIN public.user_presence p ON p.user_id = m.user_id
WHERE p.user_id IS NULL
ON CONFLICT (user_id) DO NOTHING;

-- FIX 9: Add explicit app_config read policy (removes rls_enabled_no_policy lint)
ALTER TABLE public.app_config ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "app_config_select_auth" ON public.app_config;
CREATE POLICY "app_config_select_auth"
ON public.app_config
FOR SELECT
TO authenticated
USING (true);

-- FIX 9b: Ensure user profile visibility for users participating in active study rooms.
DROP POLICY IF EXISTS "user_profiles_select_own" ON public.user_profiles;
DROP POLICY IF EXISTS "user_profiles_select_room_members" ON public.user_profiles;
DROP POLICY IF EXISTS "user_profiles_select_access" ON public.user_profiles;
CREATE POLICY "user_profiles_select_access"
ON public.user_profiles
FOR SELECT
TO authenticated
USING (
    user_id = (SELECT auth.uid())::text
    OR EXISTS (
        SELECT 1
        FROM public.study_room_members m
        JOIN public.study_rooms r ON r.id = m.room_id
        WHERE m.user_id = user_profiles.user_id
          AND r.is_active = true
    )
);

-- FIX 10: Remove broad duplicate listing policies on public avatar bucket
-- Public bucket object URLs work without broad SELECT listing policies.
DROP POLICY IF EXISTS "Avatar public read" ON storage.objects;
DROP POLICY IF EXISTS "algoviz_avatar_select" ON storage.objects;

-- Upsert needs SELECT in addition to INSERT + UPDATE.
DROP POLICY IF EXISTS "Avatar auth select own" ON storage.objects;
CREATE POLICY "Avatar auth select own"
ON storage.objects
FOR SELECT
TO authenticated
USING (
    bucket_id = 'Algoviz'
    AND name = 'profile_images/' || (SELECT auth.uid())::text || '.jpg'
);

-- Keep least-privilege object write policies.
DROP POLICY IF EXISTS "Avatar auth insert own" ON storage.objects;
CREATE POLICY "Avatar auth insert own"
ON storage.objects
FOR INSERT
TO authenticated
WITH CHECK (
    bucket_id = 'Algoviz'
    AND name = 'profile_images/' || (SELECT auth.uid())::text || '.jpg'
);

DROP POLICY IF EXISTS "Avatar auth update own" ON storage.objects;
CREATE POLICY "Avatar auth update own"
ON storage.objects
FOR UPDATE
TO authenticated
USING (
    bucket_id = 'Algoviz'
    AND name = 'profile_images/' || (SELECT auth.uid())::text || '.jpg'
)
WITH CHECK (
    bucket_id = 'Algoviz'
    AND name = 'profile_images/' || (SELECT auth.uid())::text || '.jpg'
);

DROP POLICY IF EXISTS "Avatar auth delete own" ON storage.objects;
CREATE POLICY "Avatar auth delete own"
ON storage.objects
FOR DELETE
TO authenticated
USING (
    bucket_id = 'Algoviz'
    AND name = 'profile_images/' || (SELECT auth.uid())::text || '.jpg'
);

COMMIT;

-- ============================================================
-- PART 3: POST-FIX VERIFICATION
-- ============================================================
SELECT '========== POST-FIX VERIFICATION ==========' as status;

-- Verify 1: All issues fixed
SELECT 
    'Auth users without profiles' as check,
    COUNT(*) as remaining_issues
FROM auth.users u
LEFT JOIN public.user_profiles p ON p.user_id = u.id::text
WHERE p.user_id IS NULL
UNION ALL
SELECT 'Study room members without profiles' as check,
    COUNT(*) as remaining_issues
FROM public.study_room_members m
LEFT JOIN public.user_profiles p ON p.user_id = m.user_id
WHERE p.user_id IS NULL AND m.user_id != ''
UNION ALL
SELECT 'Members with empty names' as check,
    COUNT(*) as remaining_issues
FROM public.study_room_members
WHERE user_name = '' OR user_name IS NULL
UNION ALL
SELECT 'Profiles with empty names' as check,
    COUNT(*) as remaining_issues
FROM public.user_profiles
WHERE name = '' OR name IS NULL
UNION ALL
SELECT 'Profiles with empty emails' as check,
    COUNT(*) as remaining_issues
FROM public.user_profiles
WHERE email = '' OR email IS NULL;

-- Verify 2: Data summary
SELECT 
    (SELECT COUNT(*) FROM auth.users) as total_auth_users,
    (SELECT COUNT(*) FROM public.user_profiles) as total_profiles,
    (SELECT COUNT(*) FROM public.study_rooms WHERE is_active = true) as active_rooms,
    (SELECT COUNT(*) FROM public.study_room_members) as total_members,
    (SELECT COUNT(*) FROM public.study_room_members WHERE is_online = true) as online_members;

-- Verify 3: Avatar coverage
SELECT 
    COUNT(DISTINCT user_id) as total_profiles,
    COUNT(DISTINCT CASE WHEN avatar_url IS NOT NULL THEN user_id END) as profiles_with_avatars,
    ROUND(100.0 * COUNT(DISTINCT CASE WHEN avatar_url IS NOT NULL THEN user_id END) / COUNT(DISTINCT user_id))::TEXT || '%' as avatar_coverage
FROM public.user_profiles;

-- Verify 4: RLS policies
SELECT 
    'RLS Policies Check' as status,
    COUNT(*) as policies_enabled
FROM pg_policies
WHERE tablename IN ('user_profiles', 'study_rooms', 'study_room_members', 'study_room_messages', 'user_presence')
  AND schemaname = 'public';

-- ============================================================
-- PART 4: OPTIMIZATION INDEXES
-- ============================================================

-- Add performance indexes
CREATE INDEX IF NOT EXISTS idx_user_profiles_name 
    ON public.user_profiles(name);

CREATE INDEX IF NOT EXISTS idx_user_profiles_username 
    ON public.user_profiles(username);

CREATE INDEX IF NOT EXISTS idx_user_profiles_avatar_url 
    ON public.user_profiles(avatar_url) WHERE avatar_url IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_study_room_members_room_user 
    ON public.study_room_members(room_id, user_id);

CREATE INDEX IF NOT EXISTS idx_study_room_members_online 
    ON public.study_room_members(room_id, is_online);

CREATE INDEX IF NOT EXISTS idx_study_room_messages_user 
    ON public.study_room_messages(room_id, user_id);

-- Add composite index for common queries
CREATE INDEX IF NOT EXISTS idx_user_profiles_lookup
    ON public.user_profiles(user_id, name, username, avatar_url);

-- ============================================================
-- PART 5: FINAL SUMMARY
-- ============================================================

SELECT '========== FINAL SUMMARY ==========' as status;
SELECT 'All Supabase data has been audited and fixed.' as message;
SELECT 'Verify the application is working correctly on device.' as next_step;
