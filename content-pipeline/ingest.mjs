// Ingesteur du contenu SkillForge : valide puis importe chaque parcours via l'API REST.
// Usage : node ingest.mjs [contentRoot] [--only <slug>] [--api <url>] [--reset]
//   --api    base de l'API (défaut $SF_API_URL ou http://localhost:8080)
//   --only   n'ingérer qu'un parcours
//   --reset  ignorer le manifeste de reprise (tout ré-importer)
// Login admin via $SF_ADMIN_EMAIL / $SF_ADMIN_PASSWORD (défaut : compte seed).
// Idempotent (upsert côté backend) ; un manifeste (.ingest-state.json) permet la reprise.

import { basename, dirname, join, resolve } from 'node:path'
import { existsSync, readFileSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { DEFAULT_CONTENT, listParcours, loadParcoursDocs, validateParcoursDir } from './lib/content.mjs'

const HERE = dirname(fileURLToPath(import.meta.url))
const STATE_FILE = join(HERE, '.ingest-state.json')

function parseArgs(argv) {
  const opts = {
    contentRoot: DEFAULT_CONTENT,
    only: null,
    api: process.env.SF_API_URL ?? 'http://localhost:8080',
    reset: false,
  }
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i]
    if (a === '--only') opts.only = argv[++i]
    else if (a === '--api') opts.api = argv[++i]
    else if (a === '--reset') opts.reset = true
    else opts.contentRoot = resolve(a)
  }
  return opts
}

// --- UUID v5 déterministe (stable entre exécutions → idempotence des refs authorées) ---
const NS = '5f6b2e10-0000-4000-8000-736b696c6c66'
const uuidToBytes = (u) => Buffer.from(u.replace(/-/g, ''), 'hex')
function bytesToUuid(b) {
  const h = Buffer.from(b).toString('hex')
  return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20, 32)}`
}
function uuidv5(name) {
  const hash = createHash('sha1').update(Buffer.concat([uuidToBytes(NS), Buffer.from(name, 'utf8')])).digest()
  const b = hash.subarray(0, 16)
  b[6] = (b[6] & 0x0f) | 0x50 // version 5
  b[8] = (b[8] & 0x3f) | 0x80 // variant RFC 4122
  return bytesToUuid(b)
}

const hashOf = (obj) => createHash('sha1').update(JSON.stringify(obj)).digest('hex').slice(0, 16)
const loadState = () => (existsSync(STATE_FILE) ? JSON.parse(readFileSync(STATE_FILE, 'utf8')) : {})
const saveState = (s) => writeFileSync(STATE_FILE, JSON.stringify(s, null, 2) + '\n')

async function api(base, method, path, { token, body } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch(base + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const text = await res.text()
  let json = null
  try {
    json = text ? JSON.parse(text) : null
  } catch {
    // corps non JSON
  }
  if (!res.ok || json?.error) {
    throw new Error(`${method} ${path} → ${res.status} ${json?.error?.message ?? res.statusText}`)
  }
  return json?.data ?? json
}

async function login(base) {
  const email = process.env.SF_ADMIN_EMAIL ?? 'admin@skillforge.dev'
  const password = process.env.SF_ADMIN_PASSWORD ?? 'Skillforge123!'
  const data = await api(base, 'POST', '/api/v1/auth/login', { body: { email, password } })
  if (!data?.accessToken) throw new Error('login : accessToken absent de la réponse')
  return data.accessToken
}

function sectionPayload(s, slug) {
  return {
    type: s.type,
    position: s.position,
    title: s.title ?? null,
    body: s.body ?? null,
    codeLanguage: s.codeLanguage ?? null,
    diagramFormat: s.diagramFormat ?? null,
    solution: s.solution ?? null,
    quizId: s.quizRef ? uuidv5(`quiz:${slug}:${s.quizRef}`) : null,
  }
}

async function ingestParcours(opts, dir, token, state) {
  const slug = basename(dir)
  console.log(`\n▶ ${slug}`)

  const errs = validateParcoursDir(dir)
  if (errs.length) {
    throw new Error(`validation échouée (${errs.length}) :\n    ${errs.join('\n    ')}`)
  }
  const { roadmap, contents, quizzes, challenges, exams } = loadParcoursDocs(dir)
  const base = opts.api

  const step = async (key, payload, fn) => {
    const h = hashOf(payload)
    if (!opts.reset && state[slug]?.[key] === h) {
      console.log(`  · ${key} (inchangé)`)
      return
    }
    await fn()
    state[slug] = state[slug] ?? {}
    state[slug][key] = h
    saveState(state)
    console.log(`  ✓ ${key}`)
  }

  // 1. Roadmap (le format authoré est directement compatible avec l'endpoint d'import).
  await step('roadmap', roadmap, () => api(base, 'POST', '/api/v1/roadmaps/import', { token, body: roadmap }))

  // 2. Résolution des UUID de nœuds via le graphe importé.
  const graph = await api(base, 'GET', `/api/v1/roadmaps/${slug}`, { token })
  const nodeIdBySlug = new Map(graph.nodes.map((n) => [n.slug, n.id]))

  // 3. Contenu par nœud (QUIZ : quizRef → quizId déterministe) + résolution des leçons.
  const lessonsByNode = new Map()
  for (const { data } of contents) {
    const nodeId = nodeIdBySlug.get(data.node)
    if (!nodeId) throw new Error(`nœud « ${data.node} » absent du graphe importé`)
    const body = {
      lessons: data.lessons.map((l) => ({
        slug: l.slug,
        title: l.title,
        position: l.position ?? 0,
        sections: (l.sections ?? []).map((s) => sectionPayload(s, slug)),
      })),
    }
    await step(`content:${data.node}`, { nodeId, body }, () =>
      api(base, 'POST', `/api/v1/nodes/${nodeId}/content/import`, { token, body }),
    )
    const lessons = await api(base, 'GET', `/api/v1/nodes/${nodeId}/lessons`, { token })
    lessonsByNode.set(data.node, new Map(lessons.map((l) => [l.slug, l.id])))
  }

  const lessonId = (node, lesson) => (lesson ? lessonsByNode.get(node)?.get(lesson) ?? null : null)

  // 4. Quiz (id = uuidv5(quizRef) → correspond au quizId injecté dans les sections).
  for (const { data } of quizzes) {
    const body = {
      id: uuidv5(`quiz:${slug}:${data.ref}`),
      nodeId: nodeIdBySlug.get(data.node) ?? null,
      lessonId: lessonId(data.node, data.lesson),
      title: data.title,
      description: data.description ?? null,
      questions: data.questions.map((q, qi) => ({
        type: q.type,
        prompt: q.prompt,
        justification: q.justification ?? null,
        position: q.position ?? qi,
        options: q.options.map((o, oi) => ({
          label: o.label,
          correct: o.correct === true,
          position: o.position ?? oi,
        })),
      })),
    }
    await step(`quiz:${data.ref}`, body, () => api(base, 'POST', '/api/v1/quizzes/import', { token, body }))
  }

  // 5. Défis de code.
  for (const { file, data } of challenges) {
    const body = {
      id: uuidv5(`challenge:${slug}:${data.node}:${data.title}`),
      nodeId: nodeIdBySlug.get(data.node) ?? null,
      lessonId: lessonId(data.node, data.lesson),
      title: data.title,
      statement: data.statement,
      skeleton: data.skeleton ?? null,
      solution: data.solution,
      keyPoints: data.keyPoints,
      language: data.language ?? null,
    }
    await step(`challenge:${basename(file)}`, body, () =>
      api(base, 'POST', '/api/v1/challenges/import', { token, body }),
    )
  }

  // 6. Examens (certifications). Items TASK (prompt/solution) et MCQ (question de quiz existante).
  if (exams.length) {
    const cert = await api(base, 'GET', `/api/v1/roadmaps/${slug}/certification`, { token })
    const domainIdByName = new Map(cert.domains.map((d) => [d.name, d.id]))

    // Résolution des questions MCQ : on relit le quiz importé (GET /quizzes/{id} expose les id + position).
    const questionsCache = new Map()
    const questionsOf = async (quizRef) => {
      if (!questionsCache.has(quizRef)) {
        const quizId = uuidv5(`quiz:${slug}:${quizRef}`)
        const quiz = await api(base, 'GET', `/api/v1/quizzes/${quizId}`, { token })
        const qs = [...(quiz.questions ?? [])].sort((a, b) => a.position - b.position)
        questionsCache.set(quizRef, qs)
      }
      return questionsCache.get(quizRef)
    }

    for (const { file, data } of exams) {
      const items = []
      let ix = 0
      for (const it of data.items) {
        const examDomainId = domainIdByName.get(it.domain)
        if (!examDomainId) throw new Error(`examen ${basename(file)} : domaine « ${it.domain} » introuvable`)
        if (it.kind === 'MCQ') {
          const questions = await questionsOf(it.quizRef)
          const q = questions[it.questionIndex]
          if (!q) {
            throw new Error(
              `examen ${basename(file)} : questionIndex ${it.questionIndex} introuvable dans le quiz « ${it.quizRef} »`,
            )
          }
          items.push({
            examDomainId,
            kind: 'MCQ',
            questionId: q.id,
            prompt: null,
            taskSolution: null,
            points: it.points,
            position: it.position ?? ix,
          })
        } else {
          items.push({
            examDomainId,
            kind: 'TASK',
            questionId: null,
            prompt: it.prompt,
            taskSolution: it.taskSolution,
            points: it.points,
            position: it.position ?? ix,
          })
        }
        ix++
      }
      const body = {
        id: uuidv5(`exam:${slug}:${data.title}`),
        certificationId: cert.id,
        title: data.title,
        format: data.format,
        durationMinutes: data.durationMinutes,
        passingScore: data.passingScore ?? null,
        items,
      }
      await step(`exam:${basename(file)}`, body, () => api(base, 'POST', '/api/v1/exams/import', { token, body }))
    }
  }
}

async function main() {
  const opts = parseArgs(process.argv.slice(2))
  if (!existsSync(opts.contentRoot)) {
    console.error(`Dossier de contenu introuvable : ${opts.contentRoot}`)
    process.exit(1)
  }
  const parcours = listParcours(opts.contentRoot, opts.only)
  if (parcours.length === 0) {
    console.log(`Aucun parcours à ingérer${opts.only ? ` (filtre --only ${opts.only})` : ''}.`)
    return
  }

  console.log(`API : ${opts.api}`)
  let token
  try {
    token = await login(opts.api)
  } catch (e) {
    console.error(`Échec du login admin : ${e.message}`)
    process.exit(1)
  }

  const state = loadState()
  let failures = 0
  for (const dir of parcours) {
    try {
      await ingestParcours(opts, dir, token, state)
    } catch (e) {
      failures++
      console.error(`  ✗ ${basename(dir)} : ${e.message}`)
    }
  }

  console.log(
    `\n${failures === 0 ? '✓' : '✗'} ${parcours.length - failures}/${parcours.length} parcours ingéré(s).`,
  )
  if (failures > 0) process.exit(1)
}

main()
