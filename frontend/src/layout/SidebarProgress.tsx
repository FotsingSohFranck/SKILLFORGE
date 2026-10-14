import { useGamification } from '../gamification/gamificationStore'
import { IconFlame } from '../components/icons'
import styles from './SidebarProgress.module.css'

function levelPercent(totalXp: number, minXp: number, nextMinXp: number): number {
  const span = nextMinXp - minXp
  if (span <= 0) return 100
  return Math.max(0, Math.min(100, ((totalXp - minXp) / span) * 100))
}

/** Widget de progression permanent dans la barre latérale : niveau, XP, prochain palier. */
export function SidebarProgress() {
  const profile = useGamification((s) => s.profile)
  if (!profile?.currentLevel) return null

  const percent = profile.nextLevel
    ? levelPercent(profile.totalXp, profile.currentLevel.minXp, profile.nextLevel.minXp)
    : 100

  return (
    <div className={styles.widget}>
      <div className={styles.row}>
        <span className={styles.level}>{profile.currentLevel.name}</span>
        <span className={styles.xp}>{profile.totalXp} XP</span>
      </div>
      <div className={styles.bar}>
        <span style={{ width: `${percent}%` }} />
      </div>
      {profile.nextLevel ? (
        <div className={styles.next}>
          <IconFlame size={13} /> Encore {profile.xpToNextLevel} XP → {profile.nextLevel.name}
        </div>
      ) : (
        <div className={styles.next}>Niveau maximum atteint</div>
      )}
    </div>
  )
}
