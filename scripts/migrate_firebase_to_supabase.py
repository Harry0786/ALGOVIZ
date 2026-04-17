#!/usr/bin/env python3
"""One-time migration of Firestore data to Supabase PostgREST tables.

Usage (PowerShell):
  python scripts/migrate_firebase_to_supabase.py --apply

The script reads defaults from local.properties:
- SUPABASE_URL
- SUPABASE_SERVICE_ROLE_KEY (preferred) or SUPABASE_KEY

You can override with CLI arguments.
"""

from __future__ import annotations

import argparse
import json
import os
import pathlib
import sys
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from typing import Any, Dict, Iterable, List, Optional


REPO_ROOT = pathlib.Path(__file__).resolve().parents[1]
DEFAULT_FIREBASE_SERVICE_ACCOUNT = REPO_ROOT / "algoviz-plus-firebase-adminsdk-fbsvc-beec5d2ea0.json"
DEFAULT_LOCAL_PROPERTIES = REPO_ROOT / "local.properties"


@dataclass
class Config:
    supabase_url: str
    supabase_key: str
    firebase_service_account: pathlib.Path
    dry_run: bool
    batch_size: int


def read_local_properties(path: pathlib.Path) -> Dict[str, str]:
    if not path.exists():
        return {}
    result: Dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        result[key.strip()] = value.strip()
    return result


def parse_args() -> Config:
    parser = argparse.ArgumentParser(description="Migrate Firebase Firestore data to Supabase tables")
    parser.add_argument("--firebase-service-account", default=str(DEFAULT_FIREBASE_SERVICE_ACCOUNT))
    parser.add_argument("--supabase-url", default=None)
    parser.add_argument("--supabase-key", default=None)
    parser.add_argument("--batch-size", type=int, default=500)
    parser.add_argument("--apply", action="store_true", help="Apply changes. Without this flag script runs in dry-run mode.")
    args = parser.parse_args()

    local_props = read_local_properties(DEFAULT_LOCAL_PROPERTIES)
    supabase_url = args.supabase_url or local_props.get("SUPABASE_URL", "")
    supabase_key = args.supabase_key or local_props.get("SUPABASE_SERVICE_ROLE_KEY", "") or local_props.get("SUPABASE_KEY", "")

    # Dry-run mode only validates Firestore extraction, so Supabase credentials are optional.
    if args.apply:
        if not supabase_url:
            raise SystemExit("SUPABASE_URL is required (arg or local.properties)")
        if not supabase_key:
            raise SystemExit("SUPABASE_SERVICE_ROLE_KEY or SUPABASE_KEY is required (arg or local.properties)")

    return Config(
        supabase_url=supabase_url.rstrip("/"),
        supabase_key=supabase_key,
        firebase_service_account=pathlib.Path(args.firebase_service_account),
        dry_run=not args.apply,
        batch_size=max(1, args.batch_size),
    )


def chunked(items: List[Dict[str, Any]], size: int) -> Iterable[List[Dict[str, Any]]]:
    for index in range(0, len(items), size):
        yield items[index : index + size]


def to_millis(value: Any) -> Optional[int]:
    if value is None:
        return None
    if isinstance(value, bool):
        return None
    if isinstance(value, (int, float)):
        return int(value)
    if isinstance(value, str):
        try:
            return int(float(value))
        except ValueError:
            return None

    # Firestore Timestamp and datetime-like support.
    to_dt = getattr(value, "to_datetime", None)
    if callable(to_dt):
        dt = to_dt()
        return int(dt.timestamp() * 1000)

    ts = getattr(value, "timestamp", None)
    if callable(ts):
        return int(ts() * 1000)

    return None


def to_int(value: Any, default: int = 0) -> int:
    if value is None:
        return default
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def to_bool(value: Any, default: bool = False) -> bool:
    if isinstance(value, bool):
        return value
    if value is None:
        return default
    if isinstance(value, str):
        return value.strip().lower() in {"1", "true", "yes", "y"}
    return bool(value)


def postgrest_request(config: Config, method: str, path: str, body: Any) -> None:
    url = f"{config.supabase_url}/rest/v1/{path}"
    payload = json.dumps(body).encode("utf-8")
    request = urllib.request.Request(
        url=url,
        data=payload,
        method=method,
        headers={
            "apikey": config.supabase_key,
            "Authorization": f"Bearer {config.supabase_key}",
            "Content-Type": "application/json",
            "Prefer": "resolution=merge-duplicates,return=minimal",
        },
    )
    try:
        with urllib.request.urlopen(request) as response:
            if response.status >= 300:
                raise RuntimeError(f"Supabase request failed ({response.status}) for {path}")
    except urllib.error.HTTPError as exc:
        body_text = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Supabase HTTP error {exc.code} for {path}: {body_text}") from exc


def upsert_rows(
    config: Config,
    table: str,
    rows: List[Dict[str, Any]],
    on_conflict: str,
) -> None:
    if not rows:
        print(f"[skip] {table}: no rows")
        return

    print(f"[plan] {table}: {len(rows)} rows")
    if config.dry_run:
        return

    encoded_on_conflict = urllib.parse.quote(on_conflict, safe=",")
    path = f"{table}?on_conflict={encoded_on_conflict}"
    for batch in chunked(rows, config.batch_size):
        postgrest_request(config, "POST", path, batch)
    print(f"[done] {table}: {len(rows)} rows")


def init_firestore(service_account_path: pathlib.Path):
    try:
        import firebase_admin
        from firebase_admin import credentials, firestore
    except ImportError as exc:
        raise SystemExit(
            "firebase-admin is required. Install with: pip install firebase-admin"
        ) from exc

    if not service_account_path.exists():
        raise SystemExit(f"Firebase service account file not found: {service_account_path}")

    if not firebase_admin._apps:
        cred = credentials.Certificate(str(service_account_path))
        firebase_admin.initialize_app(cred)
    return firestore.client()


def collect_firestore_data(db) -> Dict[str, List[Dict[str, Any]]]:
    study_rooms: List[Dict[str, Any]] = []
    study_room_members: List[Dict[str, Any]] = []
    study_room_messages: List[Dict[str, Any]] = []
    presence_rows: List[Dict[str, Any]] = []
    app_config_rows: List[Dict[str, Any]] = []
    profile_rows: List[Dict[str, Any]] = []

    room_docs = list(db.collection("study_rooms").stream())
    for room_doc in room_docs:
        room = room_doc.to_dict() or {}
        room_id = room_doc.id
        study_rooms.append(
            {
                "id": room_id,
                "name": room.get("name", ""),
                "description": room.get("description", ""),
                "category": room.get("category", ""),
                "created_by": room.get("createdBy", ""),
                "created_at": to_millis(room.get("createdAt")) or 0,
                "member_count": to_int(room.get("memberCount"), 0),
                "max_members": to_int(room.get("maxMembers"), 50),
                "is_private": to_bool(room.get("isPrivate"), False),
                "last_message_at": to_millis(room.get("lastMessageAt")),
                "last_message": room.get("lastMessage"),
                "is_active": to_bool(room.get("isActive"), True),
            }
        )

        member_docs = list(
            db.collection("study_rooms").document(room_id).collection("members").stream()
        )
        for member_doc in member_docs:
            member = member_doc.to_dict() or {}
            user_id = member.get("userId") or member_doc.id
            study_room_members.append(
                {
                    "room_id": room_id,
                    "user_id": user_id,
                    "user_name": member.get("userName", ""),
                    "joined_at": to_millis(member.get("joinedAt")) or 0,
                    "is_online": to_bool(member.get("isOnline"), False),
                    "last_seen_at": to_millis(member.get("lastSeenAt")),
                    "unread_count": to_int(member.get("unreadCount"), 0),
                    "is_typing": to_bool(member.get("isTyping"), False),
                    "typing_at": to_millis(member.get("typingAt")),
                }
            )

        message_docs = list(
            db.collection("study_rooms").document(room_id).collection("messages").stream()
        )
        for message_doc in message_docs:
            message = message_doc.to_dict() or {}
            study_room_messages.append(
                {
                    "id": message_doc.id,
                    "room_id": room_id,
                    "user_id": message.get("userId", ""),
                    "user_name": message.get("userName", ""),
                    "content": message.get("content", ""),
                    "type": message.get("type", "TEXT"),
                    "timestamp": to_millis(message.get("timestamp")) or 0,
                    "edited": to_bool(message.get("edited"), False),
                    "edited_at": to_millis(message.get("editedAt")),
                    "code_language": message.get("codeLanguage"),
                    "reply_to_id": message.get("replyToId"),
                    "reply_to_content": message.get("replyToContent"),
                }
            )

    for presence_doc in db.collection("user_presence").stream():
        presence = presence_doc.to_dict() or {}
        user_id = presence.get("userId") or presence_doc.id
        presence_rows.append(
            {
                "user_id": user_id,
                "is_online": to_bool(presence.get("isOnline"), False),
                "last_seen_at": to_millis(presence.get("lastSeenAt")) or 0,
            }
        )

    latest_version_doc = db.collection("app_config").document("latest_version").get()
    if latest_version_doc.exists:
        latest = latest_version_doc.to_dict() or {}
        app_config_rows.append(
            {
                "id": "latest_version",
                "version_code": to_int(latest.get("versionCode"), 0),
                "version_name": latest.get("versionName", ""),
                "apk_url": latest.get("apkUrl", ""),
                "release_notes": latest.get("releaseNotes", ""),
                "force_update": to_bool(latest.get("forceUpdate"), False),
                "updated_at": to_millis(latest.get("updatedAt")),
            }
        )

    for profile_doc in db.collection("user_profiles").stream():
        profile = profile_doc.to_dict() or {}
        profile_rows.append(
            {
                "user_id": profile_doc.id,
                "name": profile.get("name", ""),
                "email": profile.get("email", ""),
                "bio": profile.get("bio", ""),
                "avatar_url": profile.get("avatarUrl"),
                "study_goal": profile.get("studyGoal", ""),
                "skill_level": profile.get("skillLevel", ""),
                "avatar_color_index": to_int(profile.get("avatarColorIndex"), 0),
                "updated_at": to_millis(profile.get("updatedAt")),
            }
        )

    return {
        "study_rooms": study_rooms,
        "study_room_members": study_room_members,
        "study_room_messages": study_room_messages,
        "user_presence": presence_rows,
        "app_config": app_config_rows,
        "user_profiles": profile_rows,
    }


def main() -> int:
    config = parse_args()
    print("Firebase -> Supabase migration")
    print(f"Mode: {'DRY-RUN' if config.dry_run else 'APPLY'}")

    db = init_firestore(config.firebase_service_account)
    payload = collect_firestore_data(db)

    upsert_rows(config, "study_rooms", payload["study_rooms"], "id")
    upsert_rows(config, "study_room_members", payload["study_room_members"], "room_id,user_id")
    upsert_rows(config, "study_room_messages", payload["study_room_messages"], "id")
    upsert_rows(config, "user_presence", payload["user_presence"], "user_id")
    upsert_rows(config, "app_config", payload["app_config"], "id")
    upsert_rows(config, "user_profiles", payload["user_profiles"], "user_id")

    print("Migration completed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
