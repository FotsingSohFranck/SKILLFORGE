import { apiFetch } from './client'

/** Épreuve pratique d'un nœud + état de la dernière tentative de l'apprenant. */
export interface LabPractical {
  exists: boolean
  required: boolean
  title: string | null
  instructions: string | null
  expectedPath: string | null
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

/** Ticket court (usage unique) pour ouvrir un terminal interactif sur le nœud. */
export interface TerminalTicket {
  ticket: string
  expiresInSeconds: number
}

/** Demande un ticket de terminal (appel authentifié) avec la géométrie initiale souhaitée. */
export function requestTerminalTicket(nodeId: string, cols: number, rows: number): Promise<TerminalTicket> {
  return apiFetch<TerminalTicket>('POST', `/lab/nodes/${nodeId}/terminal/ticket`, { cols, rows })
}

/** Construit l'URL WebSocket du terminal (même origine que l'app) à partir d'un ticket. */
export function terminalSocketUrl(ticket: string): string {
  const proto = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${proto}://${window.location.host}/api/v1/lab/terminal?ticket=${encodeURIComponent(ticket)}`
}
