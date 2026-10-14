import { create } from 'zustand'
import { getGamifiedProfile, listBadges } from '../api/gamification'
import type { Badge, GamifiedProfile } from '../api/types'
import { useToasts } from '../components/toast/toastStore'
import { celebrate } from '../celebrate'

interface GamificationState {
  profile: GamifiedProfile | null
  badges: Badge[]
  loaded: boolean
  /** Charge le profil + badges (une fois, au démarrage de l'app privée). */
  load: () => Promise<void>
  /**
   * Rafraîchit après une activité créditée côté serveur, et déclenche les
   * célébrations en comparant l'état avant/après (XP gagnée, niveau, badges).
   */
  celebrateAfterActivity: () => Promise<void>
}

export const useGamification = create<GamificationState>((set, get) => ({
  profile: null,
  badges: [],
  loaded: false,

  load: async () => {
    try {
      const [profile, badges] = await Promise.all([getGamifiedProfile(), listBadges()])
      set({ profile, badges, loaded: true })
    } catch {
      set({ loaded: true })
    }
  },

  celebrateAfterActivity: async () => {
    const before = get().profile
    const seenBadges = new Set(get().badges.map((b) => b.code))
    try {
      const [profile, badges] = await Promise.all([getGamifiedProfile(), listBadges()])
      set({ profile, badges, loaded: true })

      // Sans état antérieur (store non chargé), on évite toute fausse célébration.
      if (!before) return
      const push = useToasts.getState().push

      const gained = profile.totalXp - before.totalXp
      if (gained > 0) push('success', `+${gained} XP`)

      if (
        before.currentLevel &&
        profile.currentLevel &&
        before.currentLevel.code !== profile.currentLevel.code
      ) {
        push('success', `Niveau atteint : ${profile.currentLevel.name}`)
        celebrate()
      }

      badges
        .filter((b) => !seenBadges.has(b.code))
        .forEach((b) => push('success', `Badge débloqué : ${b.name}`))
    } catch {
      // rafraîchissement best-effort : on n'interrompt pas le parcours
    }
  },
}))
