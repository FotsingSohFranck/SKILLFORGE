import { apiFetch } from './client'

export type ChallengeMode = 'FORMATIF' | 'CERTIFICATIF'

/** Présentation apprenant du Final Challenge d'un nœud (sans correction). */
export interface ChallengeInfo {
  available: boolean
  scenarioKey: string | null
  title: string | null
  objective: string | null
  briefing: string | null
  mode: ChallengeMode | null
  rules: string[]
}

export type GuidanceLevel = 'FAVORABLE' | 'A_SURVEILLER' | 'DEFAVORABLE'

export interface ChoiceView {
  id: string
  label: string
}

export interface NodeView {
  id: string
  title: string
  context: string | null
  event: string | null
  interactionType: string | null
  competencies: string[]
  choices: ChoiceView[]
}

export interface CompetencyScore {
  competency: string
  raw: number
  max: number
  percent: number
}

export interface ChallengeResultView {
  globalScore: number
  passed: boolean
  reinforce: boolean
  eliminated: boolean
  competencies: CompetencyScore[]
  triggeredCritical: string[]
}

export type AttemptStatus = 'IN_PROGRESS' | 'COMPLETED'

/** Entrée du journal de mission (révision) : décision déjà prise, enrichie des libellés. */
export interface DecisionReview {
  seq: number
  nodeId: string
  nodeTitle: string
  choiceId: string
  choiceLabel: string
}

/** Vue d'une tentative : nœud courant (en cours) ou rapport (terminée). Jamais de correction. */
export interface Attempt {
  attemptId: string
  status: AttemptStatus
  briefing: string | null
  objective: string | null
  mode: ChallengeMode
  rules: string[]
  lessonsAllowed: boolean
  replayAllowed: boolean
  node: NodeView | null
  guidance: GuidanceLevel | null
  history: DecisionReview[]
  result: ChallengeResultView | null
}

/** Disponibilité + présentation du Final Challenge d'un nœud. */
export function getChallengeInfo(nodeId: string): Promise<ChallengeInfo> {
  return apiFetch<ChallengeInfo>('GET', `/final-challenges/nodes/${nodeId}`)
}

/** Démarre une tentative sur le nœud (scénario publié). */
export function startChallenge(nodeId: string): Promise<Attempt> {
  return apiFetch<Attempt>('POST', '/final-challenges/attempts', { nodeId })
}

/** Tentative EN COURS de l'apprenant sur ce nœud (reprise), ou null. */
export function getCurrentAttempt(nodeId: string): Promise<Attempt | null> {
  return apiFetch<Attempt | null>('GET', `/final-challenges/nodes/${nodeId}/current-attempt`)
}

/** Lit l'état courant d'une tentative. */
export function getAttempt(attemptId: string): Promise<Attempt> {
  return apiFetch<Attempt>('GET', `/final-challenges/attempts/${attemptId}`)
}

/** Soumet une décision (choix) sur le nœud courant. */
export function submitDecision(attemptId: string, choiceId: string): Promise<Attempt> {
  return apiFetch<Attempt>('POST', `/final-challenges/attempts/${attemptId}/decisions`, { choiceId })
}
