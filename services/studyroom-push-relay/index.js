const admin = require('firebase-admin');

function required(name) {
  const value = process.env[name];
  if (!value || value.trim() === '') {
    throw new Error(`Missing required env var: ${name}`);
  }
  return value;
}

function parseServiceAccount() {
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  const base64 = process.env.FIREBASE_SERVICE_ACCOUNT_JSON_BASE64;

  if (raw && raw.trim()) {
    return JSON.parse(raw);
  }

  if (base64 && base64.trim()) {
    const decoded = Buffer.from(base64, 'base64').toString('utf8');
    return JSON.parse(decoded);
  }

  throw new Error('Set FIREBASE_SERVICE_ACCOUNT_JSON or FIREBASE_SERVICE_ACCOUNT_JSON_BASE64');
}

function topicForRoom(roomId) {
  return `study_room_${String(roomId).replace(/[^A-Za-z0-9_-]/g, '_')}`;
}

async function markProcessed(db, roomId, messageId) {
  const docRef = db.collection('push_processed_messages').doc(`${roomId}_${messageId}`);
  await docRef.set({
    processedAt: Date.now(),
    roomId,
    messageId
  }, { merge: true });
}

async function isProcessed(db, roomId, messageId) {
  const docRef = db.collection('push_processed_messages').doc(`${roomId}_${messageId}`);
  const doc = await docRef.get();
  return doc.exists;
}

async function run() {
  const projectId = required('FIREBASE_PROJECT_ID');
  const serviceAccount = parseServiceAccount();

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId
  });

  const db = admin.firestore();
  const messaging = admin.messaging();

  const startupTimestamp = Date.now();
  console.log(`[relay] Started for project=${projectId} at ${startupTimestamp}`);

  db.collectionGroup('messages').onSnapshot(
    async (snapshot) => {
      const tasks = [];

      for (const change of snapshot.docChanges()) {
        if (change.type !== 'added') {
          continue;
        }

        const message = change.doc.data() || {};
        const roomId = String(message.roomId || '');
        const messageId = String(change.doc.id || '');
        const type = String(message.type || 'TEXT').toUpperCase();
        const timestamp = Number(message.timestamp || 0);

        if (!roomId || !messageId) {
          continue;
        }

        if (!Number.isFinite(timestamp) || timestamp < startupTimestamp) {
          continue;
        }

        if (type === 'SYSTEM') {
          continue;
        }

        tasks.push((async () => {
          if (await isProcessed(db, roomId, messageId)) {
            return;
          }

          const senderName = String(message.userName || 'Member').trim() || 'Member';
          const senderId = String(message.userId || '');
          let body = String(message.content || '').trim();
          if (!body) {
            body = type === 'CODE' ? 'Shared a code snippet' : 'Sent a new message';
          }

          const payload = {
            topic: topicForRoom(roomId),
            notification: {
              title: `New message from ${senderName}`,
              body: body.slice(0, 120)
            },
            data: {
              roomId,
              senderId,
              senderName,
              type,
              title: `New message from ${senderName}`,
              body: body.slice(0, 300)
            },
            android: {
              priority: 'high',
              notification: {
                channelId: 'study_room_messages'
              }
            }
          };

          await messaging.send(payload);
          await markProcessed(db, roomId, messageId);
          console.log(`[relay] pushed room=${roomId} message=${messageId}`);
        })());
      }

      await Promise.allSettled(tasks);
    },
    (error) => {
      console.error('[relay] Firestore listener error', error);
      // Let process manager restart the service.
      process.exit(1);
    }
  );
}

run().catch((error) => {
  console.error('[relay] fatal', error);
  process.exit(1);
});
