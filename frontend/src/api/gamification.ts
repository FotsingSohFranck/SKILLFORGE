import { apiFetch } from './client'
import type { Badge, GamifiedProfile } from './types'

/** Profil gamifié (niveau, XP) de l'apprenant courant. */
export function getGamifiedProfile(): Promise<GamifiedProfile> {
  return apiFetch<GamifiedProfile>('GET', '/gamification/me')
}

/** Badges obtenus par l'apprenant courant. */
export function listBadges(): Promise<Badge[]> {
  return apiFetch<Badge[]>('GET', '/gamification/badges')
}
