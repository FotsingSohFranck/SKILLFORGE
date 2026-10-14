import { apiFetch } from './client'
import type {
  Certification,
  Lesson,
  LessonDetail,
  RoadmapGraph,
  RoadmapNode,
  RoadmapSummary,
} from './types'

/** Liste les parcours ; filtre plein texte optionnel. */
export function listRoadmaps(query?: string): Promise<RoadmapSummary[]> {
  const suffix = query && query.trim() ? `?q=${encodeURIComponent(query.trim())}` : ''
  return apiFetch<RoadmapSummary[]>('GET', `/roadmaps${suffix}`)
}

/** Obtient un parcours avec son graphe (phases, nœuds, arêtes). */
export function getRoadmap(slug: string): Promise<RoadmapGraph> {
  return apiFetch<RoadmapGraph>('GET', `/roadmaps/${encodeURIComponent(slug)}`)
}

/** Obtient un nœud par identifiant. */
export function getNode(id: string): Promise<RoadmapNode> {
  return apiFetch<RoadmapNode>('GET', `/nodes/${id}`)
}

/** Obtient la certification (et ses domaines) d'un parcours de certification. */
export function getCertification(slug: string): Promise<Certification> {
  return apiFetch<Certification>('GET', `/roadmaps/${encodeURIComponent(slug)}/certification`)
}

/** Obtient une certification par identifiant (libellés de domaines). */
export function getCertificationById(id: string): Promise<Certification> {
  return apiFetch<Certification>('GET', `/certifications/${id}`)
}

/** Liste les leçons d'un nœud. */
export function listNodeLessons(nodeId: string): Promise<Lesson[]> {
  return apiFetch<Lesson[]>('GET', `/nodes/${nodeId}/lessons`)
}

/** Obtient une leçon et ses sections. */
export function getLesson(lessonId: string): Promise<LessonDetail> {
  return apiFetch<LessonDetail>('GET', `/lessons/${lessonId}`)
}
