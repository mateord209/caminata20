// Ejemplo de proxy para Groq (Firebase Cloud Functions 2.ª gen., Node 20).
// NO está desplegado: es la propuesta descrita en docs/SEGURIDAD_GROQ.md.
const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

admin.initializeApp();
const GROQ_API_KEY = defineSecret("GROQ_API_KEY");
const DAILY_LIMIT = 20;

exports.groqProxy = onRequest({ secrets: [GROQ_API_KEY], cors: false }, async (req, res) => {
  if (req.method !== "POST") return res.status(405).send("Method not allowed");

  // 1) Autenticación: ID token de Firebase
  const header = req.get("Authorization") || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;
  if (!token) return res.status(401).send("Missing token");
  let uid;
  try {
    uid = (await admin.auth().verifyIdToken(token)).uid;
  } catch (e) {
    return res.status(401).send("Invalid token");
  }

  // 2) Límite diario por usuario
  const day = new Date().toISOString().slice(0, 10);
  const ref = admin.firestore().doc(`proxyUsage/${uid}_${day}`);
  const allowed = await admin.firestore().runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    const count = snap.exists ? snap.data().count : 0;
    if (count >= DAILY_LIMIT) return false;
    tx.set(ref, { count: count + 1, uid, day }, { merge: true });
    return true;
  });
  if (!allowed) return res.status(429).send("Daily limit reached");

  // 3) Solo se reenvían los mensajes, con modelo fijo del lado del servidor
  const messages = Array.isArray(req.body?.messages) ? req.body.messages.slice(0, 4) : [];
  const groq = await fetch("https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: { "Authorization": `Bearer ${GROQ_API_KEY.value()}`, "Content-Type": "application/json" },
    body: JSON.stringify({ model: "openai/gpt-oss-120b", messages }),
  });
  res.status(groq.status).type("application/json").send(await groq.text());
});
