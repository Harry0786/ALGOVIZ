-- Study Rooms Data Diagnostic Script
-- Run these queries to identify any data consistency issues

-- ============================================================
-- Query 1: Check all study rooms with member counts
-- ============================================================
select
    r.id,
    r.name,
    r.category,
    r.member_count as recorded_count,
    count(m.user_id) as actual_count,
    case 
        when r.member_count = count(m.user_id) then '✓ OK'
        else '✗ MISMATCH'
    end as status
from public.study_rooms r
left join public.study_room_members m on m.room_id = r.id
where r.is_active = true
group by r.id, r.name, r.category, r.member_count
order by r.name;

-- ============================================================
-- Query 2: Check members without profiles
-- ============================================================
select
    m.room_id,
    m.user_id,
    m.user_name,
    p.user_id as profile_exists
from public.study_room_members m
left join public.user_profiles p on p.user_id = m.user_id
where p.user_id is null
  and m.user_id != ''
order by m.room_id;

-- ============================================================
-- Query 3: Check members with avatars
-- ============================================================
select
    r.name as room_name,
    r.id as room_id,
    count(distinct m.user_id) as total_members,
    count(distinct case when p.avatar_url is not null then m.user_id end) as members_with_avatars,
    round(100.0 * count(distinct case when p.avatar_url is not null then m.user_id end) / count(distinct m.user_id))::text || '%' as avatar_coverage
from public.study_rooms r
join public.study_room_members m on m.room_id = r.id
left join public.user_profiles p on p.user_id = m.user_id
where r.is_active = true
group by r.id, r.name
order by room_name;

-- ============================================================
-- Query 4: Check member data integrity
-- ============================================================
select
    r.name as room_name,
    m.user_id,
    m.user_name as member_name,
    p.name as profile_name,
    p.username as profile_username,
    p.email as profile_email,
    p.avatar_url,
    m.joined_at,
    m.is_online,
    m.last_seen_at
from public.study_room_members m
join public.study_rooms r on r.id = m.room_id
left join public.user_profiles p on p.user_id = m.user_id
where r.is_active = true
order by r.name, m.user_name
limit 50;

-- ============================================================
-- Query 5: Identify problematic data patterns
-- ============================================================
select
    'Empty member names' as issue,
    count(*) as count
from public.study_room_members
where (user_name = '' or user_name is null)

union all

select
    'Members matching user_id (not synced from profile)',
    count(*)
from public.study_room_members m
where m.user_name = m.user_id

union all

select
    'Profiles missing name field',
    count(*)
from public.user_profiles
where (name = '' or name is null)

union all

select
    'Profiles missing username field',
    count(*)
from public.user_profiles
where (username = '' or username is null)

union all

select
    'Total active study rooms',
    count(*)
from public.study_rooms
where is_active = true

union all

select
    'Total active study room members',
    count(*)
from public.study_room_members m
join public.study_rooms r on r.id = m.room_id
where r.is_active = true;

-- ============================================================
-- Query 6: Check recent member joins/activity
-- ============================================================
select
    r.name as room_name,
    m.user_name,
    m.joined_at,
    m.last_seen_at,
    m.is_online,
    p.avatar_url,
    extract(epoch from now())::bigint as current_timestamp
from public.study_room_members m
join public.study_rooms r on r.id = m.room_id
left join public.user_profiles p on p.user_id = m.user_id
where r.is_active = true
order by m.joined_at desc
limit 20;

-- ============================================================
-- Query 7: Check avatar URL format consistency
-- ============================================================
select
    case
        when avatar_url like 'https://%' then 'HTTPS URL'
        when avatar_url like 'http://%' then 'HTTP URL'
        when avatar_url like '/storage/v1/%' then 'Relative path'
        when avatar_url like 'algoviz%' then 'Storage path (algoviz)'
        when avatar_url = '' then 'Empty string'
        else 'Other format'
    end as url_format,
    count(*) as count,
    max(avatar_url) as example
from public.user_profiles
where avatar_url is not null
group by url_format
order by count desc;

-- ============================================================
-- Query 8: RLS Policy Test - Can current user see shared room members?
-- ============================================================
-- Run this as an authenticated user to verify RLS:
-- This query should show profiles of users in the same study rooms
select
    p.user_id,
    p.name,
    p.username,
    p.avatar_url,
    count(distinct m.room_id) as shared_rooms
from public.user_profiles p
join public.study_room_members m_them on m_them.user_id = p.user_id
join public.study_room_members m_me on m_me.room_id = m_them.room_id
  and m_me.user_id = auth.uid()::text
group by p.user_id, p.name, p.username, p.avatar_url
limit 20;
