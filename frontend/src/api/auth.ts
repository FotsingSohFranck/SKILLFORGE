import { apiFetch, rawRequest } from './client'
import type { UserProfile } from './types'

/** Change le mot de passe de l'utilisateur connecté (vérifie l'actuel). */
export function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  return apiFetch<void>('PATCH', '/auth/password', { currentPassword, newPassword })
}

/** Met à jour le nom affiché de l'utilisateur connecté. */
export function updateDisplayName(displayName: string): Promise<UserProfile> {
  return apiFetch<UserProfile>('PATCH', '/auth/me', { displayName })
}

/** Demande un lien de réinitialisation (réponse neutre côté serveur). */
export function forgotPassword(email: string): Promise<void> {
  return rawRequest<void>('POST', '/auth/password/forgot', { email })
}

/** Définit un nouveau mot de passe à partir d'un jeton reçu par e-mail. */
export function resetPassword(token: string, newPassword: string): Promise<void> {
  return rawRequest<void>('POST', '/auth/password/reset', { token, newPassword })
}
