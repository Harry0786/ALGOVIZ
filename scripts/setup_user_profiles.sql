-- Minimal setup for profile features on a fresh Supabase project.
-- Run in Supabase Dashboard -> SQL Editor -> New query -> Run.

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

alter table public.user_profiles enable row level security;

drop policy if exists "user_profiles_select_access" on public.user_profiles;
create policy "user_profiles_select_access"
on public.user_profiles
for select
to authenticated
using (user_id = (select auth.uid())::text);

drop policy if exists "user_profiles_insert_own" on public.user_profiles;
create policy "user_profiles_insert_own"
on public.user_profiles
for insert
to authenticated
with check (user_id = (select auth.uid())::text);

drop policy if exists "user_profiles_update_own" on public.user_profiles;
create policy "user_profiles_update_own"
on public.user_profiles
for update
to authenticated
using (user_id = (select auth.uid())::text)
with check (user_id = (select auth.uid())::text);

drop policy if exists "user_profiles_delete_own" on public.user_profiles;
create policy "user_profiles_delete_own"
on public.user_profiles
for delete
to authenticated
using (user_id = (select auth.uid())::text);

create index if not exists idx_user_profiles_user_id
    on public.user_profiles(user_id);

-- Backfill profiles for users who already signed in (e.g. Google OAuth).
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
    u.id::text,
    coalesce(
        nullif(u.raw_user_meta_data ->> 'name', ''),
        nullif(u.raw_user_meta_data ->> 'full_name', ''),
        split_part(coalesce(u.email, ''), '@', 1),
        ''
    ),
    coalesce(
        nullif(u.raw_user_meta_data ->> 'username', ''),
        nullif(u.raw_user_meta_data ->> 'user_name', ''),
        split_part(coalesce(u.email, ''), '@', 1),
        ''
    ),
    coalesce(u.email, ''),
    coalesce(u.phone, ''),
    coalesce(
        nullif(u.raw_user_meta_data ->> 'avatarUrl', ''),
        nullif(u.raw_user_meta_data ->> 'avatar_url', ''),
        nullif(u.raw_user_meta_data ->> 'picture', '')
    ),
    coalesce((u.raw_user_meta_data ->> 'avatarColorIndex')::integer, 0),
    extract(epoch from now())::bigint
from auth.users u
on conflict (user_id) do update
set
    name = excluded.name,
    username = excluded.username,
    email = excluded.email,
    avatar_url = excluded.avatar_url,
    updated_at = excluded.updated_at;

-- Auto-create profile rows for new sign-ups.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
declare
  v_user_id text;
  v_email text;
begin
  v_user_id := new.id::text;
  v_email := coalesce(new.email, '');

  insert into public.user_profiles (
    user_id,
    name,
    username,
    email,
    phone_no,
    avatar_url,
    avatar_color_index,
    updated_at
  ) values (
    v_user_id,
    coalesce(
      nullif(new.raw_user_meta_data ->> 'name', ''),
      nullif(new.raw_user_meta_data ->> 'full_name', ''),
      split_part(v_email, '@', 1),
      ''
    ),
    coalesce(
      nullif(new.raw_user_meta_data ->> 'username', ''),
      nullif(new.raw_user_meta_data ->> 'user_name', ''),
      split_part(v_email, '@', 1),
      ''
    ),
    v_email,
    coalesce(new.phone, ''),
    coalesce(
      nullif(new.raw_user_meta_data ->> 'avatarUrl', ''),
      nullif(new.raw_user_meta_data ->> 'avatar_url', ''),
      nullif(new.raw_user_meta_data ->> 'picture', '')
    ),
    coalesce((new.raw_user_meta_data ->> 'avatarColorIndex')::integer, 0),
    extract(epoch from now())::bigint
  )
  on conflict (user_id) do nothing;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();
