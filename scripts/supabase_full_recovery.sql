-- Full Supabase recovery script for AlgoViz
-- Safe to run multiple times (idempotent).

begin;

-- ------------------------------------------------------------
-- Core tables
-- ------------------------------------------------------------

create table if not exists public.study_rooms (
    id text primary key,
    name text not null,
    description text not null default '',
    category text not null default '',
    created_by text not null,
    created_at bigint not null,
    member_count integer not null default 0,
    max_members integer not null default 50,
    is_private boolean not null default false,
    last_message_at bigint,
    last_message text,
    is_active boolean not null default true
);

create table if not exists public.study_room_members (
    room_id text not null references public.study_rooms(id) on delete cascade,
    user_id text not null,
    user_name text not null,
    joined_at bigint not null,
    is_online boolean not null default false,
    last_seen_at bigint,
    unread_count integer not null default 0,
    is_typing boolean not null default false,
    typing_at bigint,
    primary key (room_id, user_id)
);

create table if not exists public.study_room_messages (
    id text primary key,
    room_id text not null references public.study_rooms(id) on delete cascade,
    user_id text not null,
    user_name text not null,
    content text not null,
    type text not null default 'TEXT',
    timestamp bigint not null,
    edited boolean not null default false,
    edited_at bigint,
    code_language text,
    reply_to_id text,
    reply_to_content text
);

create table if not exists public.user_presence (
    user_id text primary key,
    is_online boolean not null default false,
    last_seen_at bigint not null
);

create table if not exists public.app_config (
    id text primary key,
    version_code integer not null,
    version_name text not null,
    apk_url text not null,
    release_notes text not null default '',
    force_update boolean not null default false,
    updated_at bigint
);

create table if not exists public.user_profiles (
    user_id text primary key,
    name text not null default '',
    username text not null default '',
    email text not null default '',
    phone_no text not null default '',
    avatar_url text,
    avatar_color_index integer not null default 0,
    updated_at bigint
);

alter table public.user_profiles
    add column if not exists phone_no text not null default '';

alter table public.user_profiles
    add column if not exists username text not null default '';

-- ------------------------------------------------------------
-- Indexes
-- ------------------------------------------------------------

create index if not exists idx_study_room_members_user_id
    on public.study_room_members(user_id);

create index if not exists idx_study_room_messages_room_timestamp
    on public.study_room_messages(room_id, timestamp);

-- ------------------------------------------------------------
-- Recover user_profiles from auth.users metadata
-- ------------------------------------------------------------

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
select
    u.id::text as user_id,
    coalesce(
        nullif(u.raw_user_meta_data ->> 'name', ''),
        nullif(u.raw_user_meta_data ->> 'full_name', ''),
        nullif(u.raw_user_meta_data ->> 'user_name', ''),
        nullif(u.raw_user_meta_data ->> 'preferred_username', ''),
        split_part(coalesce(u.email, ''), '@', 1),
        ''
    ) as name,
    coalesce(
        nullif(u.raw_user_meta_data ->> 'username', ''),
        nullif(u.raw_user_meta_data ->> 'user_name', ''),
        nullif(u.raw_user_meta_data ->> 'preferred_username', ''),
        split_part(coalesce(u.email, ''), '@', 1),
        ''
    ) as username,
    coalesce(u.email, '') as email,
    coalesce(
        nullif(u.raw_user_meta_data ->> 'phoneNumber', ''),
        nullif(u.raw_user_meta_data ->> 'phone', ''),
        coalesce(u.phone, ''),
        ''
    ) as phone_no,
    nullif(coalesce(u.raw_user_meta_data ->> 'avatarUrl', ''), '') as avatar_url,
    coalesce((u.raw_user_meta_data ->> 'avatarColorIndex')::integer, 0) as avatar_color_index,
    extract(epoch from now())::bigint as updated_at
from auth.users u
on conflict (user_id) do update
set
    name = excluded.name,
    username = excluded.username,
    email = excluded.email,
    phone_no = excluded.phone_no,
    avatar_url = excluded.avatar_url,
    avatar_color_index = excluded.avatar_color_index,
    updated_at = excluded.updated_at;

-- ------------------------------------------------------------
-- RLS for user_profiles
-- ------------------------------------------------------------

alter table public.user_profiles enable row level security;

drop policy if exists "user_profiles_select_own" on public.user_profiles;
create policy "user_profiles_select_own"
on public.user_profiles
for select
to authenticated
using (user_id = auth.uid()::text);

drop policy if exists "user_profiles_insert_own" on public.user_profiles;
create policy "user_profiles_insert_own"
on public.user_profiles
for insert
to authenticated
with check (user_id = auth.uid()::text);

drop policy if exists "user_profiles_update_own" on public.user_profiles;
create policy "user_profiles_update_own"
on public.user_profiles
for update
to authenticated
using (user_id = auth.uid()::text)
with check (user_id = auth.uid()::text);

drop policy if exists "user_profiles_delete_own" on public.user_profiles;
create policy "user_profiles_delete_own"
on public.user_profiles
for delete
to authenticated
using (user_id = auth.uid()::text);

-- ------------------------------------------------------------
-- RLS for study room flows
-- ------------------------------------------------------------

alter table public.study_rooms enable row level security;
alter table public.study_room_members enable row level security;
alter table public.study_room_messages enable row level security;
alter table public.user_presence enable row level security;

drop policy if exists "study_rooms_select_all_active" on public.study_rooms;
create policy "study_rooms_select_all_active"
on public.study_rooms
for select
to authenticated
using (is_active = true);

drop policy if exists "study_rooms_insert_creator" on public.study_rooms;
create policy "study_rooms_insert_creator"
on public.study_rooms
for insert
to authenticated
with check (created_by = auth.uid()::text);

drop policy if exists "study_rooms_update_members" on public.study_rooms;
create policy "study_rooms_update_members"
on public.study_rooms
for update
to authenticated
using (
    created_by = auth.uid()::text
    or exists (
        select 1
        from public.study_room_members m
        where m.room_id = study_rooms.id
          and m.user_id = auth.uid()::text
    )
)
with check (
    created_by = auth.uid()::text
    or exists (
        select 1
        from public.study_room_members m
        where m.room_id = study_rooms.id
          and m.user_id = auth.uid()::text
    )
);

drop policy if exists "study_room_members_select_auth" on public.study_room_members;
create policy "study_room_members_select_auth"
on public.study_room_members
for select
to authenticated
using (true);

drop policy if exists "study_room_members_insert_self_or_admin" on public.study_room_members;
create policy "study_room_members_insert_self_or_admin"
on public.study_room_members
for insert
to authenticated
with check (
    user_id = auth.uid()::text
    or exists (
        select 1
        from public.study_rooms r
        where r.id = study_room_members.room_id
          and r.created_by = auth.uid()::text
    )
);

drop policy if exists "study_room_members_update_room_members" on public.study_room_members;
create policy "study_room_members_update_room_members"
on public.study_room_members
for update
to authenticated
using (
    user_id = auth.uid()::text
    or exists (
        select 1
        from public.study_room_members me
        where me.room_id = study_room_members.room_id
          and me.user_id = auth.uid()::text
    )
)
with check (
    user_id = auth.uid()::text
    or exists (
        select 1
        from public.study_room_members me
        where me.room_id = study_room_members.room_id
          and me.user_id = auth.uid()::text
    )
);

drop policy if exists "study_room_members_delete_self_or_admin" on public.study_room_members;
create policy "study_room_members_delete_self_or_admin"
on public.study_room_members
for delete
to authenticated
using (
    user_id = auth.uid()::text
    or exists (
        select 1
        from public.study_rooms r
        where r.id = study_room_members.room_id
          and r.created_by = auth.uid()::text
    )
);

drop policy if exists "study_room_messages_select_auth" on public.study_room_messages;
create policy "study_room_messages_select_auth"
on public.study_room_messages
for select
to authenticated
using (true);

drop policy if exists "study_room_messages_insert_sender" on public.study_room_messages;
create policy "study_room_messages_insert_sender"
on public.study_room_messages
for insert
to authenticated
with check (user_id = auth.uid()::text);

drop policy if exists "study_room_messages_update_own" on public.study_room_messages;
create policy "study_room_messages_update_own"
on public.study_room_messages
for update
to authenticated
using (user_id = auth.uid()::text)
with check (user_id = auth.uid()::text);

drop policy if exists "study_room_messages_delete_own" on public.study_room_messages;
create policy "study_room_messages_delete_own"
on public.study_room_messages
for delete
to authenticated
using (user_id = auth.uid()::text);

drop policy if exists "user_presence_select_auth" on public.user_presence;
create policy "user_presence_select_auth"
on public.user_presence
for select
to authenticated
using (true);

drop policy if exists "user_presence_insert_own" on public.user_presence;
create policy "user_presence_insert_own"
on public.user_presence
for insert
to authenticated
with check (user_id = auth.uid()::text);

drop policy if exists "user_presence_update_own" on public.user_presence;
create policy "user_presence_update_own"
on public.user_presence
for update
to authenticated
using (user_id = auth.uid()::text)
with check (user_id = auth.uid()::text);

drop policy if exists "user_presence_delete_own" on public.user_presence;
create policy "user_presence_delete_own"
on public.user_presence
for delete
to authenticated
using (user_id = auth.uid()::text);

-- ------------------------------------------------------------
-- Storage bucket and policies for avatars
-- ------------------------------------------------------------

insert into storage.buckets (id, name, public)
values ('Algoviz', 'Algoviz', true)
on conflict (id) do update set public = excluded.public;

-- Recreate policies safely
drop policy if exists "Avatar public read" on storage.objects;
create policy "Avatar public read"
on storage.objects
for select
to public
using (bucket_id = 'Algoviz');

drop policy if exists "Avatar auth insert own" on storage.objects;
create policy "Avatar auth insert own"
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'Algoviz'
    and name = 'profile_images/' || auth.uid()::text || '.jpg'
);

drop policy if exists "Avatar auth update own" on storage.objects;
create policy "Avatar auth update own"
on storage.objects
for update
to authenticated
using (
    bucket_id = 'Algoviz'
    and name = 'profile_images/' || auth.uid()::text || '.jpg'
)
with check (
    bucket_id = 'Algoviz'
    and name = 'profile_images/' || auth.uid()::text || '.jpg'
);

drop policy if exists "Avatar auth delete own" on storage.objects;
create policy "Avatar auth delete own"
on storage.objects
for delete
to authenticated
using (
    bucket_id = 'Algoviz'
    and name = 'profile_images/' || auth.uid()::text || '.jpg'
);

commit;
