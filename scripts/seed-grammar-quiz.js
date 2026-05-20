const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const GRAMMAR_COLLECTION = "grammar";
const GRAMMAR_QUESTIONS_COLLECTION = "grammar_questions";
const QUESTION_LIMIT = 10;
const AUTO_SOURCE = "seed-grammar-quiz";

const dryRun = process.argv.includes("--dry-run");

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
  } catch (error) {
    return "";
  }
}

function text(value) {
  return typeof value === "string" ? value.trim() : "";
}

function firstText(source, fields) {
  for (const field of fields) {
    const value = text(source[field]);
    if (value) return value;
  }
  return "";
}

function asList(value) {
  return Array.isArray(value) ? value : [];
}

function asMap(value) {
  return value && typeof value === "object" && !Array.isArray(value) ? value : {};
}

function toLesson(doc) {
  const data = doc.data();
  const id = firstText(data, ["grammar_id", "id"]) || doc.id;
  const title = firstText(data, ["title", "pattern"]);
  const examples = asList(data.examples)
    .map(asMap)
    .map((item) => ({
      jp: firstText(item, ["jp", "jpText", "text"]),
      vi: firstText(item, ["vi", "viText", "meaning_vi", "translation"]),
    }))
    .filter((item) => item.jp || item.vi);

  return {
    id,
    title,
    level: firstText(data, ["level"]) || "N5",
    meaning: firstText(data, ["meaning_vi", "meaning"]),
    formation: firstText(data, ["structure", "formation"]),
    usage: [firstText(data, ["usage"]), firstText(data, ["note"])]
      .filter(Boolean)
      .join("\n\n"),
    status: firstText(data, ["status"]),
    embeddedQuestions: asList(data.questions || data.quiz),
    examples,
  };
}

function toQuestionKey(question) {
  const content = text(question.content || question.question).toLowerCase();
  const answer = text(question.correctAnswer || question.answer).toLowerCase();
  return `${content}|${answer}`;
}

function hash(value) {
  let result = 0;
  for (let index = 0; index < value.length; index += 1) {
    result = (result * 31 + value.charCodeAt(index)) >>> 0;
  }
  return result;
}

function unique(values) {
  const seen = new Set();
  return values
    .map(text)
    .filter(Boolean)
    .filter((value) => {
      const key = value.toLowerCase();
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
}

function buildOptions(correctAnswer, pool, seed) {
  const options = unique([correctAnswer, ...pool]).slice(0, 4);
  if (options.length < 4) return [];
  const offset = hash(seed) % options.length;
  return options.slice(offset).concat(options.slice(0, offset));
}

function question({ lesson, type, content, correctAnswer, optionPool, hint, suffix }) {
  const options = buildOptions(correctAnswer, optionPool, `${lesson.id}_${type}_${suffix || content}`);
  if (!text(content) || !text(correctAnswer) || options.length < 4) return null;

  const id = `${lesson.id}_auto_${type.toLowerCase()}_${hash(content).toString(36)}`;
  return {
    id,
    lessonId: lesson.id,
    grammarId: lesson.id,
    type,
    content,
    options,
    correctAnswer,
    hint: hint || `Ôn lại phần giải thích của ${lesson.title}.`,
    generatedBy: AUTO_SOURCE,
  };
}

function buildQuestionCandidates(lesson, lessons) {
  const otherLessons = lessons.filter((item) => item.id !== lesson.id);
  const titlePool = otherLessons.map((item) => item.title);
  const meaningPool = otherLessons.map((item) => item.meaning);
  const formationPool = otherLessons.map((item) => item.formation);
  const usagePool = otherLessons.map((item) => item.usage);
  const exampleJpPool = otherLessons.flatMap((item) => item.examples.map((example) => example.jp));
  const exampleViPool = otherLessons.flatMap((item) => item.examples.map((example) => example.vi));

  const candidates = [
    question({
      lesson,
      type: "MEANING_TO_PATTERN",
      content: `Mẫu ngữ pháp nào phù hợp với nghĩa: "${lesson.meaning}"?`,
      correctAnswer: lesson.title,
      optionPool: titlePool,
      hint: "Nhìn vào nghĩa tiếng Việt rồi chọn mẫu câu tương ứng.",
    }),
    question({
      lesson,
      type: "PATTERN_MEANING",
      content: `"${lesson.title}" thường dùng để diễn đạt gì?`,
      correctAnswer: lesson.meaning,
      optionPool: meaningPool,
      hint: "Chọn nghĩa gần nhất với cách dùng của mẫu.",
    }),
    question({
      lesson,
      type: "FORMATION",
      content: `Cấu trúc nào đúng cho "${lesson.title}"?`,
      correctAnswer: lesson.formation,
      optionPool: formationPool,
      hint: "So sánh phần đứng trước và sau mẫu ngữ pháp.",
    }),
    question({
      lesson,
      type: "FILL_STRUCTURE",
      content: `Điền mẫu ngữ pháp còn thiếu trong cấu trúc: ${lesson.formation.replace(lesson.title, "____") || "____"}`,
      correctAnswer: lesson.title,
      optionPool: titlePool,
      hint: "Chọn mẫu khớp với cấu trúc của bài.",
    }),
    question({
      lesson,
      type: "USAGE",
      content: `Cách dùng nào đúng với "${lesson.title}"?`,
      correctAnswer: lesson.usage,
      optionPool: usagePool,
      hint: "Đọc kỹ lưu ý dùng của mẫu ngữ pháp.",
    }),
  ];

  lesson.examples.slice(0, 4).forEach((example, index) => {
    candidates.push(
      question({
        lesson,
        type: "EXAMPLE_SENTENCE",
        content: `Câu nào là ví dụ đúng cho "${lesson.title}"?`,
        correctAnswer: example.jp,
        optionPool: exampleJpPool,
        hint: "Chọn câu có cách dùng đúng theo mẫu.",
        suffix: `jp_${index}`,
      }),
      question({
        lesson,
        type: "EXAMPLE_TRANSLATION",
        content: `Bản dịch nào đúng cho câu: "${example.jp}"?`,
        correctAnswer: example.vi,
        optionPool: exampleViPool,
        hint: "Đối chiếu nghĩa của câu ví dụ.",
        suffix: `vi_${index}`,
      }),
      question({
        lesson,
        type: "MEANING_TO_SENTENCE",
        content: `Câu nào diễn đạt nghĩa: "${example.vi}"?`,
        correctAnswer: example.jp,
        optionPool: exampleJpPool,
        hint: "Chọn câu tiếng Nhật khớp với nghĩa tiếng Việt.",
        suffix: `meaning_${index}`,
      })
    );
  });

  return candidates.filter(Boolean);
}

async function loadExternalQuestions(db, lessonId) {
  const fields = ["lessonId", "grammarId", "grammar_id"];
  const snapshots = await Promise.all(
    fields.map((field) =>
      db.collection(GRAMMAR_QUESTIONS_COLLECTION).where(field, "==", lessonId).get()
    )
  );
  const docs = snapshots.flatMap((snapshot) => snapshot.docs);
  const byPath = new Map(docs.map((doc) => [doc.ref.path, doc]));
  return Array.from(byPath.values()).map((doc) => doc.data());
}

async function seedLesson(db, lesson, lessons) {
  const externalQuestions = await loadExternalQuestions(db, lesson.id);
  const existingQuestions = [...lesson.embeddedQuestions, ...externalQuestions];
  const existingKeys = new Set(existingQuestions.map(toQuestionKey).filter((key) => key !== "|"));
  const missingCount = Math.max(0, QUESTION_LIMIT - existingKeys.size);

  if (missingCount === 0) {
    return { lessonId: lesson.id, added: 0, existing: existingKeys.size, skipped: false };
  }

  const candidates = buildQuestionCandidates(lesson, lessons)
    .filter((candidate) => !existingKeys.has(toQuestionKey(candidate)));
  const uniqueCandidates = [];
  const candidateKeys = new Set();

  for (const candidate of candidates) {
    const key = toQuestionKey(candidate);
    if (candidateKeys.has(key)) continue;
    candidateKeys.add(key);
    uniqueCandidates.push(candidate);
  }

  const additions = uniqueCandidates.slice(0, missingCount);
  if (additions.length < missingCount) {
    return {
      lessonId: lesson.id,
      added: 0,
      existing: existingKeys.size,
      skipped: true,
      reason: `Only built ${additions.length}/${missingCount} missing questions from lesson data.`,
    };
  }

  if (!dryRun) {
    const batch = db.batch();
    additions.forEach((item) => {
      const ref = db.collection(GRAMMAR_QUESTIONS_COLLECTION).doc(item.id);
      batch.set(
        ref,
        {
          ...item,
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        },
        { merge: true }
      );
    });
    await batch.commit();
  }

  return { lessonId: lesson.id, added: additions.length, existing: existingKeys.size, skipped: false };
}

async function main() {
  initFirebase();
  const db = admin.firestore();
  const grammarSnapshot = await db.collection(GRAMMAR_COLLECTION).get();
  const lessons = grammarSnapshot.docs
    .map(toLesson)
    .filter((lesson) => lesson.title && lesson.status !== "unpublished");

  const results = [];
  for (const lesson of lessons) {
    results.push(await seedLesson(db, lesson, lessons));
  }

  const added = results.reduce((sum, item) => sum + item.added, 0);
  const skipped = results.filter((item) => item.skipped);
  const prefix = dryRun ? "[dry-run]" : "[seed]";

  console.log(`${prefix} processed ${results.length} grammar lessons; ${added} questions ${dryRun ? "would be added" : "added"}.`);

  if (skipped.length > 0) {
    console.warn(`${prefix} ${skipped.length} lessons still need manual questions:`);
    skipped.forEach((item) => {
      console.warn(`- ${item.lessonId}: ${item.reason}`);
    });
    process.exitCode = 1;
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
