import { useEffect, useRef, useState, type CSSProperties } from 'react'
import { useAuth } from '../auth/authStore'
import { Avatar } from '../components/Avatar'
import { ThemeToggle } from '../components/ThemeToggle'
import { IconLogout } from '../components/icons'
import { useGamification } from '../gamification/gamificationStore'
import styles from './Header.module.css'

/** Pourcentage de progression vers le prochain niveau. */
function levelPercent(totalXp: number, minXp: number, nextMinXp: number): number {
  const span = nextMinXp - minXp
  if (span <= 0) return 100
  return Math.max(0, Math.min(100, ((totalXp - minXp) / span) * 100))
}

/** En-tête : pastille niveau & XP, puis menu utilisateur (avatar → thème, déconnexion). */
export function Header() {
  const user = useAuth((s) => s.user)
  const logout = useAuth((s) => s.logout)
  const profile = useGamification((s) => s.profile)
  const loaded = useGamification((s) => s.loaded)
  const load = useGamification((s) => s.load)
  const [menuOpen, setMenuOpen] = useState(false)
  // Hairline façon Apple : le filet sous l'en-tête n'apparaît qu'une fois le contenu défilé dessous.
  const [scrolled, setScrolled] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)
  // Ouverture du menu au survol (avec petit délai de fermeture pour pouvoir y entrer).
  const closeTimer = useRef<number | null>(null)
  function openMenu() {
    if (closeTimer.current) {
      clearTimeout(closeTimer.current)
      closeTimer.current = null
    }
    setMenuOpen(true)
  }
  function scheduleClose() {
    closeTimer.current = window.setTimeout(() => setMenuOpen(false), 180)
  }

  useEffect(() => {
    if (!loaded) void load()
  }, [loaded, load])

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 2)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  // Fermeture du menu au clic extérieur et sur Échap.
  useEffect(() => {
    if (!menuOpen) return
    function onDown(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) setMenuOpen(false)
    }
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') setMenuOpen(false)
    }
    document.addEventListener('mousedown', onDown)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDown)
      document.removeEventListener('keydown', onKey)
    }
  }, [menuOpen])

  const percent =
    profile?.currentLevel && profile.nextLevel
      ? levelPercent(profile.totalXp, profile.currentLevel.minXp, profile.nextLevel.minXp)
      : null

  return (
    <header className={[styles.header, scrolled ? styles.scrolled : ''].filter(Boolean).join(' ')}>
      {profile?.currentLevel && (
        <div className={styles.pill}>
          <span className={styles.level}>{profile.currentLevel.name}</span>
          <span className={styles.xp}>{profile.totalXp} XP</span>
          {percent != null && (
            <span
              className={styles.bar}
              role="progressbar"
              aria-valuenow={Math.round(percent)}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label={`Progression vers ${profile?.nextLevel?.name ?? 'niveau suivant'}`}
            >
              <span className={styles.fill} style={{ '--sf-p': `${percent}%` } as CSSProperties} />
            </span>
          )}
          {profile?.nextLevel && (
            <span className={styles.next}>
              {profile.xpToNextLevel} XP → {profile.nextLevel.name}
            </span>
          )}
        </div>
      )}

      <div className={styles.menu} ref={menuRef} onMouseEnter={openMenu} onMouseLeave={scheduleClose}>
        <button
          type="button"
          className={`${styles.trigger} sf-pressable`}
          onClick={() => setMenuOpen((v) => !v)}
          aria-haspopup="menu"
          aria-expanded={menuOpen}
        >
          <Avatar name={user?.displayName ?? '?'} size={32} />
          <span className={styles.name}>{user?.displayName}</span>
        </button>

        {menuOpen && (
          <div className={styles.dropdown} role="menu">
            <div className={styles.account}>
              <div className={styles.accountName}>{user?.displayName}</div>
              <div className={styles.accountEmail}>{user?.email}</div>
            </div>
            <div className={styles.menuRow}>
              <span>Thème</span>
              <ThemeToggle />
            </div>
            <button
              type="button"
              className={styles.logout}
              role="menuitem"
              onClick={() => void logout()}
            >
              <IconLogout size={16} /> Déconnexion
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
