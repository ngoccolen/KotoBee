const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const USERS_COLLECTION = "users";
const ACTIVITY_COLLECTION = "study_activity";
const TOTAL_POINTS_FIELD = "totalStudyPoints";
const BACKFILLED_AT_FIELD = "leaderboardBackfilledAt";
const DEFAULT_BATCH_SIZE = 400;

const dryRun = process.argv.includes("--dry-run") || !process.argv.includes("--write");
const batchSize = numberArg("--batch-size", DEFAULT_BATCH_SIZE);

function initFirebase() {
  if (admin.apps.length > 0) return;
  const projectId = resolveProjectId();
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    ...(projectId ? { projectId } : {}),
  });
}

function resolveProjectId() {
  if (process.env.GOOGLE_CLOUD_PROJECT) return process.env.GOOGLE_CLOUD_PROJECT;
  if (process.env.GCLOUD_PROJECT) return process.env.GCLOUD_PROJECT;
  if (process.env.FIREBASE_PROJECT_ID) return process.env.FIREBASE_PROJECT_ID;

  const googleServicesPath = path.resolve(__dirname, "../app/google-services.json");
  if (!fs.existsSync(googleServicesPath)) return "";

  try {
    const config = JSON.parse(fs.readFileSync(googleServicesPath, "utf8"));
    return text(config.project_info && config.project_info.project_id);
  } catch (_error) {
    return "";
  }
}

function text(value) {
  return typeof value === "string" ? value.trim() : "";
}

function numberArg(flag, fallback) {
  const index = process.argv.indexOf(flag);
  if (index === -1) return fallback;
  const value = Number(process.argv[index + 1]);
  return Number.isInteger(value) && value > 0 ? value : fallback;
}

function numberField(source, fields) {
  for (const field of fields) {
    const value = source[field];
    if (typeof value === "number" && Number.isFinite(value)) {
      return { found: true, value };
    }
    if (typeof value === "string") {
      const parsed = Number(value);
      if (Number.isFinite(parsed)) return { found: true, value: parsed };
    }
  }
  return { found: false, value: 0 };
}

function activityPoints(data) {
  return numberField(data, ["value", "points"]).value;
}

async function commitBatch(batch, pendingWrites) {
  if (pendingWrites === 0) return 0;
  await batch.commit();
  return pendingWrites;
}

async function main() {
  initFirebase();

  const db = admin.firestore();
  const usersSnapshot = await db.collection(USERS_COLLECTION).get();
  let batch = db.batch();
  let pendingWrites = 0;
  let committedWrites = 0;
  let changedUsers = 0;
  let totalPoints = 0;

  console.log(`Mode: ${dryRun ? "dry-run" : "write"}`);
  console.log(`Users found: ${usersSnapshot.size}`);

  for (const userDoc of usersSnapshot.docs) {
    const activitySnapshot = await userDoc.ref.collection(ACTIVITY_COLLECTION).get();
    const computedTotal = activitySnapshot.docs.reduce((sum, activityDoc) => {
      return sum + activityPoints(activityDoc.data());
    }, 0);

    const current = numberField(userDoc.data(), [TOTAL_POINTS_FIELD]);
    const shouldUpdate = !current.found || current.value !== computedTotal;
    totalPoints += computedTotal;

    if (!shouldUpdate) continue;

    changedUsers += 1;
    console.log(
      `${userDoc.id}: ${current.found ? current.value : "missing"} -> ${computedTotal}`
    );

    if (dryRun) continue;

    batch.set(
      userDoc.ref,
      {
        [TOTAL_POINTS_FIELD]: computedTotal,
        [BACKFILLED_AT_FIELD]: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    pendingWrites += 1;
    if (pendingWrites >= batchSize) {
      committedWrites += await commitBatch(batch, pendingWrites);
      batch = db.batch();
      pendingWrites = 0;
    }
  }

  if (!dryRun) {
    committedWrites += await commitBatch(batch, pendingWrites);
  }

  console.log(`Users needing update: ${changedUsers}`);
  console.log(`Computed total points: ${totalPoints}`);
  if (dryRun) {
    console.log("No writes were made. Run with --write to update Firestore.");
  } else {
    console.log(`Writes committed: ${committedWrites}`);
  }
}

function isMissingCredentialError(error) {
  const message = String(error && (error.message || error.stack || error));
  return message.includes("Could not load the default credentials");
}

main().catch((error) => {
  if (isMissingCredentialError(error)) {
    console.error("Missing Firebase Admin credentials.");
    console.error("Set GOOGLE_APPLICATION_CREDENTIALS to a service-account JSON file, or run:");
    console.error("  gcloud auth application-default login");
    console.error("Then rerun: npm run backfill:leaderboard:dry");
  } else {
    console.error(error);
  }
  process.exit(1);
});
