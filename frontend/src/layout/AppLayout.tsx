import { Fragment, useLayoutEffect, useRef, useState } from 'react'
import type { CSSProperties, ReactNode } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/authStore'
import { CommandPalette } from '../components/CommandPalette'
import { Confetti } from '../components/Confetti'
import { IconBook, IconHome, IconPaths, IconSearch, IconShield, IconUser } from '../components/icons'
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

/** Groupes de navigation, séparés par un filet dans la barre latérale. */
const NAV_MAIN: NavItem[] = [
  { to: '/', label: 'Accueil', icon: (a) => <IconHome size={18} filled={a} />, end: true },
  { to: '/catalogue', label: 'Catalogue', icon: (a) => <IconBook size={18} filled={a} /> },
]
const NAV_LEARN: NavItem[] = [
  { to: '/mes-parcours', label: 'Mes parcours', icon: (a) => <IconPaths size={18} filled={a} /> },
]
const NAV_ACCOUNT: NavItem[] = [
  { to: '/profile', label: 'Profil', icon: (a) => <IconUser size={18} filled={a} /> },
]

function linkClass({ isActive }: { isActive: boolean }): string {
  return [styles.link, 'sf-pressable', isActive ? styles.active : ''].filter(Boolean).join(' ')
}

/** Cadre applicatif privé : barre latérale + en-tête + zone de contenu (Outlet). */
export function AppLayout() {
  const isAdmin = useAuth((s) => s.hasRole('ADMIN'))
  const location = useLocation()
  const navRef = useRef<HTMLElement>(null)
  // Indicateur de sélection glissant : on MESURE la position réelle du lien actif (robuste aux
  // séparateurs et au nombre variable d'items) plutôt que de calculer un index x pas fixe.
  const [indicator, setIndicator] = useState({ top: 0, height: 0, visible: false })

  useLayoutEffect(() => {
    const nav = navRef.current
    if (!nav) return
    const active = nav.querySelector<HTMLElement>(`a.${styles.active}`)
    if (active) {
      setIndicator({ top: active.offsetTop, height: active.offsetHeight, visible: true })
    } else {
      setIndicator((prev) => ({ ...prev, visible: false }))
    }
  }, [location.pathname, isAdmin])

  const accountGroup = isAdmin
    ? [
        ...NAV_ACCOUNT,
        { to: '/admin', label: 'Admin', icon: (a: boolean) => <IconShield size={18} filled={a} /> },
      ]
    : NAV_ACCOUNT
  const groups: NavItem[][] = [NAV_MAIN, NAV_LEARN, accountGroup]

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

        <nav className={styles.nav} ref={navRef}>
          <span
            className={styles.navIndicator}
            data-visible={indicator.visible}
            aria-hidden="true"
            style={
              { '--ind-top': `${indicator.top}px`, '--ind-h': `${indicator.height}px` } as CSSProperties
            }
          />
          {groups.map((group, groupIndex) => (
            <Fragment key={groupIndex}>
              {groupIndex > 0 && <div className={styles.sep} aria-hidden="true" />}
              <div className={styles.group}>
                {group.map((item) => (
                  <NavLink key={item.to} to={item.to} end={item.end} className={linkClass}>
                    {({ isActive }) => (
                      <>
                        {item.icon(isActive)}
                        <span>{item.label}</span>
                      </>
                    )}
                  </NavLink>
                ))}
              </div>
            </Fragment>
          ))}
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
