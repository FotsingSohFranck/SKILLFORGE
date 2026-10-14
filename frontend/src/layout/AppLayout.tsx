import type { CSSProperties, ReactNode } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/authStore'
import { CommandPalette } from '../components/CommandPalette'
import { Confetti } from '../components/Confetti'
import { IconBook, IconHome, IconSearch, IconShield, IconUser } from '../components/icons'
import { OnboardingTour } from '../components/OnboardingTour'
import { ToastViewport } from '../components/toast/ToastViewport'
import { Header } from './Header'
import { SidebarProgress } from './SidebarProgress'
import styles from './AppLayout.module.css'

interface NavItem {
  to: string
  label: string
  /** L'icône reçoit l'état actif pour basculer en variante pleine (façon SF Symbols). */
  icon: (active: boolean) => ReactNode
  end?: boolean
}

const NAV: NavItem[] = [
  { to: '/', label: 'Accueil', icon: (a) => <IconHome size={18} filled={a} />, end: true },
  { to: '/catalogue', label: 'Catalogue', icon: (a) => <IconBook size={18} filled={a} /> },
  { to: '/profile', label: 'Profil', icon: (a) => <IconUser size={18} filled={a} /> },
]

function linkClass({ isActive }: { isActive: boolean }): string {
  return [styles.link, 'sf-pressable', isActive ? styles.active : ''].filter(Boolean).join(' ')
}

/**
 * Index de l'item de nav actif (ordre : Accueil, Catalogue, Profil, Admin), ou -1 si la route
 * ne correspond a aucun item. Pilote l'indicateur de sélection glissant via --active-index,
 * posé DIRECTEMENT sur l'indicateur (variable enregistrée @property -> transition fluide).
 */
function navActiveIndex(pathname: string): number {
  if (pathname === '/') return 0
  if (pathname.startsWith('/catalogue')) return 1
  if (pathname.startsWith('/profile')) return 2
  if (pathname.startsWith('/admin')) return 3
  return -1
}

/** Cadre applicatif privé : barre latérale + en-tête + zone de contenu (Outlet). */
export function AppLayout() {
  const isAdmin = useAuth((s) => s.hasRole('ADMIN'))
  const location = useLocation()
  const activeIndex = navActiveIndex(location.pathname)

  return (
    <div className={styles.shell}>
      <a href="#main" className={styles.skipLink}>
        Aller au contenu
      </a>
      <aside className={styles.sidebar}>
        <div className={styles.brand}>
          <span className={styles.logo} aria-hidden="true">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none">
              <path d="M12 2 3 7v10l9 5 9-5V7l-9-5Z" fill="var(--color-primary)" />
              <path d="M12 7 7 9.5v5L12 17l5-2.5v-5L12 7Z" fill="#fff" opacity="0.9" />
            </svg>
          </span>
          <span>SkillForge</span>
        </div>

        <button
          type="button"
          className={`${styles.search} sf-pressable`}
          onClick={() =>
            window.dispatchEvent(new KeyboardEvent('keydown', { key: 'k', ctrlKey: true }))
          }
        >
          <span className={styles.searchLabel}>
            <IconSearch size={16} /> Rechercher…
          </span>
          <kbd className={styles.kbd}>Ctrl K</kbd>
        </button>

        <nav className={styles.nav}>
          <span
            className={styles.navIndicator}
            data-visible={activeIndex >= 0}
            aria-hidden="true"
            style={{ '--active-index': Math.max(0, activeIndex) } as CSSProperties}
          />
          {NAV.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end} className={linkClass}>
              {({ isActive }) => (
                <>
                  {item.icon(isActive)}
                  <span>{item.label}</span>
                </>
              )}
            </NavLink>
          ))}
          {isAdmin && (
            <NavLink to="/admin" className={linkClass}>
              {({ isActive }) => (
                <>
                  <IconShield size={18} filled={isActive} />
                  <span>Admin</span>
                </>
              )}
            </NavLink>
          )}
        </nav>

        <SidebarProgress />
      </aside>
      <div className={styles.content}>
        <Header />
        <main id="main" className={styles.main} tabIndex={-1}>
          <Outlet />
        </main>
      </div>
      <ToastViewport />
      <CommandPalette />
      <OnboardingTour />
      <Confetti />
    </div>
  )
}
