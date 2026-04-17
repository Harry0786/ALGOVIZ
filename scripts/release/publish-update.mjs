import process from "node:process";

function required(name) {
  const value = process.env[name];
  if (!value || value.trim() === "") {
    throw new Error(`Missing required environment variable: ${name}`);
  }
  return value;
}

function optional(name) {
  const value = process.env[name];
  if (!value || value.trim() === "") {
    return null;
  }
  return value;
}

async function publishToSupabase({
  supabaseUrl,
  supabaseServiceRoleKey,
  versionCode,
  versionName,
  apkUrl,
  releaseNotes,
  forceUpdate
}) {
  if (!supabaseUrl || !supabaseServiceRoleKey) {
    return false;
  }

  const endpoint = `${supabaseUrl.replace(/\/$/, "")}/rest/v1/app_config?on_conflict=id`;
  const payload = [
    {
      id: "latest_version",
      version_code: versionCode,
      version_name: versionName,
      apk_url: apkUrl,
      release_notes: releaseNotes,
      force_update: forceUpdate,
      updated_at: Date.now()
    }
  ];

  const response = await fetch(endpoint, {
    method: "POST",
    headers: {
      apikey: supabaseServiceRoleKey,
      Authorization: `Bearer ${supabaseServiceRoleKey}`,
      "Content-Type": "application/json",
      Prefer: "resolution=merge-duplicates,return=minimal"
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`Supabase publish failed (${response.status}): ${body}`);
  }

  return true;
}

async function main() {
  const apkPublicUrl = required("APK_PUBLIC_URL");
  const versionCode = Number(required("VERSION_CODE"));
  const versionName = required("VERSION_NAME");
  const releaseNotes = process.env.RELEASE_NOTES?.trim() || `Automated release ${versionName}`;
  const forceUpdate = String(process.env.FORCE_UPDATE || "false").toLowerCase() === "true";
  const supabaseUrl = optional("SUPABASE_URL");
  const supabaseServiceRoleKey = optional("SUPABASE_SERVICE_ROLE_KEY");

  if (!Number.isInteger(versionCode) || versionCode <= 0) {
    throw new Error("VERSION_CODE must be a positive integer");
  }

  const publishedToSupabase = await publishToSupabase({
    supabaseUrl,
    supabaseServiceRoleKey,
    versionCode,
    versionName,
    apkUrl: apkPublicUrl,
    releaseNotes,
    forceUpdate
  });

  console.log("Published update metadata successfully.");
  console.log(`APK URL: ${apkPublicUrl}`);
  console.log(`Supabase metadata published: ${publishedToSupabase}`);

  if (!publishedToSupabase) {
    throw new Error("No update metadata destination succeeded (Supabase unavailable)");
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
