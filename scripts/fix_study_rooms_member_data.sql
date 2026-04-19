-- Fix Study Rooms Member Data Consistency
-- This script ensures all study room members have:
-- 1. Corresponding user_profiles entries
-- 2. Updated member names from user profiles
-- 3. Avatar URLs properly populated

-- ============================================================
-- Step 1: Create missing user_profiles entries for orphan members
-- ============================================================

insert into public.user_profiles (
    user_id,
    name,
    username,
    email,
    phone_no,
    avatar_url,
    avatar_color_index,
    updated_at
)
select distinct
    m.user_id,
    m.user_name,  -- Use existing member name as fallback
    split_part(m.user_name, ' ', 1),  -- Use first name as username
    'missing-' || m.user_id || '@algoviz.local',  -- Placeholder email
    '',
    null,
    0,
    extract(epoch from now())::bigint
from public.study_room_members m
left join public.user_profiles p on p.user_id = m.user_id
where p.user_id is null
  and m.user_id != ''
on conflict (user_id) do nothing;

-- ============================================================
-- Step 2: Sync member names from user_profiles where names are empty
-- ============================================================

update public.study_room_members m
set user_name = coalesce(p.name, p.username, split_part(p.email, '@', 1), m.user_name)
from public.user_profiles p
where m.user_id = p.user_id
  and (m.user_name = '' or m.user_name = m.user_id)
  and p.name != '';

-- ============================================================
-- Step 3: Verify all study room members have valid profiles
-- ============================================================

select 
    'Orphan members (no profile)' as issue,
    count(*) as count
from public.study_room_members m
left join public.user_profiles p on p.user_id = m.user_id
where p.user_id is null
  and m.user_id != ''

union all

select
    'Empty member names' as issue,
    count(*) as count
from public.study_room_members
where user_name = '' or user_name is null

union all

select
    'Profiles with avatars' as issue,
    count(*) as count
from public.user_profiles
where avatar_url is not null and avatar_url != '';

-- ============================================================
-- Step 4: Add index optimization for profile lookups
-- ============================================================

create index if not exists idx_user_profiles_name on public.user_profiles(name);
create index if not exists idx_user_profiles_avatar_url on public.user_profiles(avatar_url) where avatar_url is not null;
create index if not exists idx_study_room_members_room_user on public.study_room_members(room_id, user_id);

-- ============================================================
-- Summary
-- ============================================================

-- Run these queries to verify the fix:
-- 1. Check member profiles exist:
--    SELECT m.user_id, COUNT(*) as member_count
--    FROM public.study_room_members m
--    LEFT JOIN public.user_profiles p ON p.user_id = m.user_id
--    GROUP BY m.user_id
--    HAVING p.user_id IS NULL;
--
-- 2. Check avatars are populated:
--    SELECT COUNT(*) as members_with_avatars
--    FROM public.study_room_members m
--    JOIN public.user_profiles p ON p.user_id = m.user_id
--    WHERE p.avatar_url IS NOT NULL;
--
-- 3. Check member display names:
--    SELECT m.user_id, m.user_name, p.name, p.username
--    FROM public.study_room_members m
--    LEFT JOIN public.user_profiles p ON p.user_id = m.user_id
--    LIMIT 10;
