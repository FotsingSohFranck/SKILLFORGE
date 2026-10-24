import { apiFetch } from './client'

/** Épreuve pratique d'un nœud + état de la dernière tentative de l'apprenant. */
export interface LabPractical {
  exists: boolean
  required: boolean
  title: string | null
  instructions: string | null
  latestStatus: string | null
  latestPassed: boolean
}

/** Fichier soumis par l'apprenant. */
export interface LabFile {
  path: string
  content: string
}

/** Verdict d'une soumission pratique. */
export interface LabVerdict {
  attemptId: string
  passed: boolean
  output: string
}

/** Consulte l'épreuve pratique d'un nœud (existence, consigne, statut courant). */
export function getPractical(nodeId: string): Promise<LabPractical> {
  return apiFetch<LabPractical>('GET', `/lab/nodes/${nodeId}/practical`)
}

/** Soumet les fichiers de l'apprenant pour correction ; renvoie le verdict. */
export function submitPractical(nodeId: string, files: LabFile[]): Promise<LabVerdict> {
  return apiFetch<LabVerdict>('POST', `/lab/nodes/${nodeId}/submit`, { files })
}
