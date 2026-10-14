import { apiFetch } from './client'
import type {
  AttemptResult,
  Challenge,
  ChallengeSummary,
  DomainResult,
  Exam,
  ExamAttempt,
  ExamItemAnswerInput,
  ExamResult,
  ExamSummary,
  Quiz,
  SubmissionResult,
  SubmittedAnswer,
} from './types'

/** Obtient un quiz pour passage (sans les bonnes réponses). */
export function getQuiz(quizId: string): Promise<Quiz> {
  return apiFetch<Quiz>('GET', `/quizzes/${quizId}`)
}

/** Soumet une tentative de quiz ; renvoie la correction immédiate. */
export function submitQuizAttempt(quizId: string, answers: SubmittedAnswer[]): Promise<AttemptResult> {
  return apiFetch<AttemptResult>('POST', `/quizzes/${quizId}/attempts`, { answers })
}

/** Liste les défis de code rattachés à un nœud. */
export function listNodeChallenges(nodeId: string): Promise<ChallengeSummary[]> {
  return apiFetch<ChallengeSummary[]>('GET', `/nodes/${nodeId}/challenges`)
}

/** Obtient un défi de code pour résolution (sans solution ni points clés). */
export function getChallenge(challengeId: string): Promise<Challenge> {
  return apiFetch<Challenge>('GET', `/challenges/${challengeId}`)
}

/** Soumet du code pour un défi ; renvoie points clés atteints/manquants + solution. */
export function submitChallenge(challengeId: string, code: string): Promise<SubmissionResult> {
  return apiFetch<SubmissionResult>('POST', `/challenges/${challengeId}/submit`, { code })
}

/** Liste les examens blancs d'une certification. */
export function listCertificationExams(certificationId: string): Promise<ExamSummary[]> {
  return apiFetch<ExamSummary[]>('GET', `/certifications/${certificationId}/exams`)
}

/** Obtient un examen pour passage (items sans bonnes réponses). */
export function getExam(examId: string): Promise<Exam> {
  return apiFetch<Exam>('GET', `/exams/${examId}`)
}

/** Démarre une tentative d'examen (positionne la deadline / minuteur). */
export function startExam(examId: string): Promise<ExamAttempt> {
  return apiFetch<ExamAttempt>('POST', `/exams/${examId}/attempts`)
}

/** Soumet une tentative d'examen ; renvoie le résultat par domaine. */
export function submitExam(attemptId: string, answers: ExamItemAnswerInput[]): Promise<ExamResult> {
  return apiFetch<ExamResult>('POST', `/exams/attempts/${attemptId}/submit`, { answers })
}

/** Historique des tentatives d'un examen pour l'apprenant courant (plus récentes d'abord). */
export function getExamHistory(examId: string): Promise<ExamAttempt[]> {
  return apiFetch<ExamAttempt[]>('GET', `/exams/${examId}/attempts`)
}

/** Domaines faibles agrégés (révision ciblée) sur les tentatives d'un examen. */
export function getExamRevision(examId: string): Promise<DomainResult[]> {
  return apiFetch<DomainResult[]>('GET', `/exams/${examId}/revision`)
}
