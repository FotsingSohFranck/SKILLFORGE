import type { Streak } from '../api/types'

export type StreakStatus = 'done-today' | 'at-risk' | 'inactive'

function todayKey(): string {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Statut de la série pour aujourd'hui, déduit de `lastActivityDate` :
 * - `done-today` : une activité a déjà été faite aujourd'hui (série sécurisée) ;
 * - `at-risk` : série en cours mais rien fait aujourd'hui (à préserver) ;
 * - `inactive` : aucune série en cours.
 */
export function streakStatus(streak: Streak): StreakStatus {
  const active = streak.currentLength > 0
  if (streak.lastActivityDate && streak.lastActivityDate.slice(0, 10) === todayKey()) {
    return 'done-today'
  }
  return active ? 'at-risk' : 'inactive'
}
