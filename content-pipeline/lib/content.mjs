// Chargement + validation du contenu pivot authoré. Partagé par validate.mjs et ingest.mjs.

import { readFileSync, readdirSync, existsSync, statSync } from 'node:fs'
import { join, dirname, resolve, basename } from 'node:path'
import { fileURLToPath } from 'node:url'
import Ajv from 'ajv'

const HERE = dirname(fileURLToPath(import.meta.url))
const SCHEMA_DIR = join(HERE, '..', 'schemas')
export const DEFAULT_CONTENT = resolve(HERE, '..', '..', 'content')

const ajv = new Ajv({ allErrors: true })
const loadSchema = (name) => JSON.parse(readFileSync(join(SCHEMA_DIR, name), 'utf8'))
const validators = {
  roadmap: ajv.compile(loadSchema('roadmap.schema.json')),
  nodeContent: ajv.compile(loadSchema('node-content.schema.json')),
  quiz: ajv.compile(loadSchema('quiz.schema.json')),
  challenge: ajv.compile(loadSchema('challenge.schema.json')),
  exam: ajv.compile(loadSchema('exam.schema.json')),
  practical: ajv.compile(loadSchema('practical.schema.json')),
}

export function readJson(file) {
  return JSON.parse(readFileSync(file, 'utf8'))
}

export const listJson = (dir) =>
  existsSync(dir) ? readdirSync(dir).filter((f) => f.endsWith('.json')).map((f) => join(dir, f)) : []

export function listParcours(contentRoot, only) {
  if (!existsSync(contentRoot)) return []
  return readdirSync(contentRoot)
    .map((name) => join(contentRoot, name))
    .filter((p) => statSync(p).isDirectory())
    .filter((p) => !only || basename(p) === only)
}

function dup(values) {
  const seen = new Set()
  const dups = new Set()
  for (const v of values) (seen.has(v) ? dups : seen).add(v)
  return [...dups]
}

/** Charge les documents d'un parcours (sans valider). Lève sur JSON invalide. */
export function loadParcoursDocs(dir) {
  const roadmap = readJson(join(dir, 'roadmap.json'))
  const contents = listJson(join(dir, 'nodes')).map((file) => ({ file, data: readJson(file) }))
  const quizzes = listJson(join(dir, 'quizzes')).map((file) => ({ file, data: readJson(file) }))
  const challenges = listJson(join(dir, 'challenges')).map((file) => ({ file, data: readJson(file) }))
  const exams = listJson(join(dir, 'exams')).map((file) => ({ file, data: readJson(file) }))
  const practicals = listJson(join(dir, 'practical')).map((file) => ({ file, data: readJson(file) }))
  return { slug: basename(dir), roadmap, contents, quizzes, challenges, exams, practicals }
}

/** Valide un parcours (structure ajv + cohérence inter-fichiers). Renvoie la liste d'erreurs. */
export function validateParcoursDir(dir) {
  const errors = []
  const fail = (file, msg) => errors.push(`${file}: ${msg}`)
  const readOr = (file) => {
    try {
      return readJson(file)
    } catch (e) {
      fail(file, `JSON invalide — ${e.message}`)
      return null
    }
  }
  const structural = (file, data, validator) => {
    if (validator(data)) return true
    for (const e of validator.errors) fail(file, `${e.instancePath || '/'} ${e.message}`)
    return false
  }

  const roadmapFile = join(dir, 'roadmap.json')
  if (!existsSync(roadmapFile)) {
    fail(dir, 'roadmap.json manquant')
    return errors
  }
  const roadmap = readOr(roadmapFile)
  if (!roadmap) return errors
  structural(roadmapFile, roadmap, validators.roadmap)

  const nodeSlugs = new Set((roadmap.nodes ?? []).map((n) => n.slug))
  const phaseTitles = new Set((roadmap.phases ?? []).map((p) => p.title))
  const domainNames = new Set((roadmap.certification?.domains ?? []).map((d) => d.name))

  for (const s of dup((roadmap.nodes ?? []).map((n) => n.slug))) fail(roadmapFile, `slug de nœud dupliqué : ${s}`)
  for (const t of dup((roadmap.phases ?? []).map((p) => p.title))) fail(roadmapFile, `titre de phase dupliqué : ${t}`)
  for (const n of roadmap.nodes ?? []) {
    if (n.phase && !phaseTitles.has(n.phase)) fail(roadmapFile, `nœud ${n.slug} : phase inconnue « ${n.phase} »`)
    for (const ed of n.examDomains ?? []) {
      if (!domainNames.has(ed.domain)) fail(roadmapFile, `nœud ${n.slug} : domaine d'examen inconnu « ${ed.domain} »`)
    }
  }
  for (const e of roadmap.edges ?? []) {
    if (!nodeSlugs.has(e.source)) fail(roadmapFile, `arête : source inconnue « ${e.source} »`)
    if (!nodeSlugs.has(e.target)) fail(roadmapFile, `arête : cible inconnue « ${e.target} »`)
    if (e.source === e.target) fail(roadmapFile, `arête : source == cible (${e.source})`)
  }
  const isCert = roadmap.kind === 'CERTIFICATION'
  if (isCert && !roadmap.certification) fail(roadmapFile, 'kind=CERTIFICATION mais bloc certification absent')
  if (!isCert && roadmap.certification) fail(roadmapFile, 'bloc certification présent mais kind ≠ CERTIFICATION')

  const quizByRef = new Map()
  for (const file of listJson(join(dir, 'quizzes'))) {
    const quiz = readOr(file)
    if (!quiz || !structural(file, quiz, validators.quiz)) continue
    if (quizByRef.has(quiz.ref)) fail(file, `ref de quiz dupliquée : ${quiz.ref}`)
    quizByRef.set(quiz.ref, quiz)
    if (!nodeSlugs.has(quiz.node)) fail(file, `nœud inconnu « ${quiz.node} »`)
    for (const q of quiz.questions ?? []) {
      if (!(q.options ?? []).some((o) => o.correct === true)) {
        fail(file, `question « ${q.prompt.slice(0, 40)}… » : aucune option correcte`)
      }
    }
  }

  const contentNodes = []
  for (const file of listJson(join(dir, 'nodes'))) {
    const content = readOr(file)
    if (!content || !structural(file, content, validators.nodeContent)) continue
    contentNodes.push(content.node)
    if (!nodeSlugs.has(content.node)) fail(file, `nœud inconnu « ${content.node} »`)
    for (const s of dup((content.lessons ?? []).map((l) => l.slug))) fail(file, `slug de leçon dupliqué : ${s}`)
    for (const lesson of content.lessons ?? []) {
      for (const p of dup((lesson.sections ?? []).map((sec) => sec.position))) {
        fail(file, `leçon ${lesson.slug} : position de section dupliquée : ${p}`)
      }
      for (const sec of lesson.sections ?? []) {
        if (sec.type === 'QUIZ') {
          if (!sec.quizRef) fail(file, `leçon ${lesson.slug} : section QUIZ sans quizRef`)
          else if (!quizByRef.has(sec.quizRef)) fail(file, `leçon ${lesson.slug} : quizRef inconnu « ${sec.quizRef} »`)
        }
        if (sec.type === 'CODE' && !sec.codeLanguage) {
          fail(file, `leçon ${lesson.slug} : section CODE sans codeLanguage`)
        }
      }
    }
  }
  for (const n of dup(contentNodes)) fail(dir, `plusieurs fichiers de contenu pour le nœud « ${n} »`)

  for (const file of listJson(join(dir, 'challenges'))) {
    const challenge = readOr(file)
    if (!challenge || !structural(file, challenge, validators.challenge)) continue
    if (!nodeSlugs.has(challenge.node)) fail(file, `nœud inconnu « ${challenge.node} »`)
  }

  for (const file of listJson(join(dir, 'exams'))) {
    const exam = readOr(file)
    if (!exam || !structural(file, exam, validators.exam)) continue
    if (!isCert) fail(file, 'examen présent mais le parcours n’est pas une CERTIFICATION')
    for (const p of dup((exam.items ?? []).map((it) => it.position))) fail(file, `position d'item dupliquée : ${p}`)
    for (const it of exam.items ?? []) {
      if (!domainNames.has(it.domain)) fail(file, `item : domaine inconnu « ${it.domain} »`)
      if (it.kind === 'MCQ') {
        if (!it.quizRef || it.questionIndex == null) fail(file, 'item MCQ : quizRef + questionIndex requis')
        else {
          const quiz = quizByRef.get(it.quizRef)
          if (!quiz) fail(file, `item MCQ : quizRef inconnu « ${it.quizRef} »`)
          else if (it.questionIndex >= (quiz.questions?.length ?? 0)) {
            fail(file, `item MCQ : questionIndex ${it.questionIndex} hors bornes (quiz ${it.quizRef})`)
          }
        }
      }
      if (it.kind === 'TASK' && (!it.prompt || !it.taskSolution)) {
        fail(file, 'item TASK : prompt + taskSolution requis')
      }
    }
  }

  const practicalNodes = []
  for (const file of listJson(join(dir, 'practical'))) {
    const practical = readOr(file)
    if (!practical || !structural(file, practical, validators.practical)) continue
    practicalNodes.push(practical.node)
    if (!nodeSlugs.has(practical.node)) fail(file, `nœud inconnu « ${practical.node} »`)
  }
  for (const n of dup(practicalNodes)) fail(dir, `plusieurs épreuves pratiques pour le nœud « ${n} »`)

  return errors
}
