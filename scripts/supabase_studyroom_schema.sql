-- Study room and migration support schema for Supabase
-- Run this in Supabase SQL editor before executing scripts/migrate_firebase_to_supabase.py.

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

create index if not exists idx_study_room_members_user_id
    on public.study_room_members(user_id);

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

create index if not exists idx_study_room_messages_room_timestamp
    on public.study_room_messages(room_id, timestamp);

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
