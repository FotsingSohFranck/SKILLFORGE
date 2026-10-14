import { apiFetch } from './client'
import type {
  Bookmark,
  BookmarkTargetType,
  Enrollment,
  NodeLessonProgress,
  Resume,
  RoadmapProgress,
  Streak,
  ToggleResult,
} from './types'

interface Activity {
  roadmapId: string
  nodeId: string
  lessonId: string
}

/** Inscrit l'apprenant courant à un parcours (idempotent côté serveur). */
export function enroll(roadmapId: string): Promise<Enrollment> {
  return apiFetch<Enrollment>('POST', '/learning/enrollments', { roadmapId })
}

/** Liste les inscriptions de l'apprenant courant (tableau de bord). */
export function listEnrollments(): Promise<Enrollment[]> {
  return apiFetch<Enrollment[]>('GET', '/learning/enrollments')
}

/** Marque une leçon comme vue (met à jour le point de reprise). */
export function markViewed(activity: Activity): Promise<void> {
  return apiFetch<void>('POST', '/learning/viewed', activity)
}

/** Marque une leçon comme terminée (déclenche progression + gamification). */
export function markCompleted(activity: Activity): Promise<void> {
  return apiFetch<void>('POST', '/learning/completed', activity)
}

/** Progression d'un parcours (par nœud) pour l'apprenant courant. */
export function getRoadmapProgress(roadmapId: string): Promise<RoadmapProgress> {
  return apiFetch<RoadmapProgress>('GET', `/learning/roadmaps/${roadmapId}/progress`)
}

/** Identifiants des leçons complétées par l'apprenant courant dans un nœud. */
export function getNodeLessonProgress(nodeId: string): Promise<NodeLessonProgress> {
  return apiFetch<NodeLessonProgress>('GET', `/learning/nodes/${nodeId}/lessons/progress`)
}

/** Point de reprise globale (dernière activité), ou null si aucune. */
export function getResume(): Promise<Resume | null> {
  return apiFetch<Resume | null>('GET', '/learning/resume')
}

/** Série d'apprentissage courante. */
export function getStreak(): Promise<Streak> {
  return apiFetch<Streak>('GET', '/learning/streak')
}

/** Liste des favoris de l'apprenant courant. */
export function listBookmarks(): Promise<Bookmark[]> {
  return apiFetch<Bookmark[]>('GET', '/learning/bookmarks')
}

/** Bascule un favori (nœud ou leçon) ; renvoie l'état résultant. */
export function toggleBookmark(targetType: BookmarkTargetType, targetId: string): Promise<ToggleResult> {
  return apiFetch<ToggleResult>('POST', '/learning/bookmarks', { targetType, targetId })
}
