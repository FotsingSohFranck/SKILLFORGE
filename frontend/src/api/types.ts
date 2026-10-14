/** Enveloppe de réponse standard du backend. */
export interface ApiResponse<T> {
  data: T | null
  meta: unknown | null
  error: ApiErrorBody | null
}

export interface FieldError {
  field: string
  message: string
}

export interface ApiErrorBody {
  code: string
  message: string
  details: FieldError[]
}

/** Erreur d'API portant le code métier et les détails de validation. */
export class ApiError extends Error {
  readonly code: string
  readonly details: FieldError[]

  constructor(body: ApiErrorBody) {
    super(body.message)
    this.name = 'ApiError'
    this.code = body.code
    this.details = body.details ?? []
  }
}

/** DTO d'authentification. */
export interface Tokens {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
}

export interface UserProfile {
  id: string
  email: string
  displayName: string
  enabled: boolean
  roles: string[]
}

export interface LevelInfo {
  code: string
  name: string
  minXp: number
}

export interface GamifiedProfile {
  totalXp: number
  currentLevel: LevelInfo | null
  nextLevel: LevelInfo | null
  xpToNextLevel: number
}

export interface Badge {
  code: string
  name: string
  description: string | null
  icon: string | null
  earnedAt: string | null
}

/* --- Catalogue --- */
export interface RoadmapSummary {
  id: string
  slug: string
  title: string
  summary: string
  kind: string
  position: number
}

export interface Phase {
  id: string
  title: string
  position: number
}

export interface RoadmapNode {
  id: string
  roadmapId: string
  phaseId: string
  slug: string
  title: string
  summary: string
  importance: string
  estimatedMinutes: number | null
  position: number
}

export interface Edge {
  id: string
  sourceNodeId: string
  targetNodeId: string
  kind: string
}

export interface RoadmapGraph {
  roadmap: RoadmapSummary
  phases: Phase[]
  nodes: RoadmapNode[]
  edges: Edge[]
}

export interface ExamDomainInfo {
  id: string
  name: string
  weight: number
  position: number
}

export interface Certification {
  id: string
  code: string
  examFormat: string
  durationMinutes: number
  passingScore: number | null
  domains: ExamDomainInfo[]
}

/* --- Contenu --- */
export interface Lesson {
  id: string
  nodeId: string
  slug: string
  title: string
  position: number
}

export type SectionType = 'TEXT' | 'CODE' | 'DIAGRAM' | 'CALLOUT' | 'QUIZ' | string

export interface Section {
  id: string
  type: SectionType
  position: number
  title: string | null
  body: string | null
  codeLanguage: string | null
  diagramFormat: string | null
  solution: string | null
  quizId: string | null
}

export interface LessonDetail {
  lesson: Lesson
  sections: Section[]
}

/* --- Apprentissage --- */
export interface Enrollment {
  id: string
  roadmapId: string
  progressPercent: number
  lastActiveNodeId: string | null
  lastActiveLessonId: string | null
}

export type NodeStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | string

export interface NodeProgress {
  nodeId: string
  status: NodeStatus
  percent: number
  lastLessonId: string | null
}

export interface RoadmapProgress {
  enrollment: Enrollment
  nodes: NodeProgress[]
}

export interface NodeLessonProgress {
  completedLessonIds: string[]
}

export interface Resume {
  roadmapId: string
  nodeId: string
  lessonId: string
}

export interface Streak {
  currentLength: number
  longestLength: number
  lastActivityDate: string | null
}

export type BookmarkTargetType = 'NODE' | 'LESSON'

export interface Bookmark {
  targetType: BookmarkTargetType
  targetId: string
}

export interface ToggleResult {
  bookmarked: boolean
}

/* --- Évaluation (quiz) --- */
export interface QuizOption {
  id: string
  label: string
  position: number
}

export interface QuizQuestion {
  id: string
  type: string
  prompt: string
  position: number
  options: QuizOption[]
}

export interface Quiz {
  id: string
  title: string
  description: string | null
  questions: QuizQuestion[]
}

export interface SubmittedAnswer {
  questionId: string
  optionId: string
}

export interface AnswerResult {
  questionId: string
  selectedOptionId: string | null
  correct: boolean
  correctOptionId: string
  justification: string | null
}

export interface AttemptResult {
  quizId: string
  score: number
  maxScore: number
  answers: AnswerResult[]
}

/* --- Évaluation (défis de code) --- */
export interface ChallengeSummary {
  id: string
  title: string
  language: string | null
}

export interface Challenge {
  id: string
  title: string
  statement: string
  skeleton: string | null
  language: string | null
}

export interface SubmissionResult {
  passed: boolean
  matched: string[]
  missing: string[]
  solution: string
}

/* --- Évaluation (examens blancs) --- */
export interface ExamSummary {
  id: string
  title: string
  format: string
  durationMinutes: number
}

export interface ExamItem {
  id: string
  domainId: string
  kind: 'MCQ' | 'TASK' | string
  prompt: string
  points: number
  position: number
  options: QuizOption[]
}

export interface Exam {
  id: string
  certificationId: string
  title: string
  format: string
  durationMinutes: number
  items: ExamItem[]
}

export interface ExamAttempt {
  id: string
  examId: string
  startedAt: string
  deadlineAt: string | null
  submittedAt: string | null
  totalScore: number
  maxScore: number
  passed: boolean | null
  status: string
}

export interface ExamItemAnswerInput {
  itemId: string
  selectedOptionId: string | null
  completed: boolean | null
}

export interface DomainResult {
  domainId: string
  score: number
  maxScore: number
  weak: boolean
}

export interface ExamResult {
  attemptId: string
  totalScore: number
  maxScore: number
  passed: boolean
  expired: boolean
  domains: DomainResult[]
}
