import { useEffect, useRef, useState } from 'react'
import { flushSync } from 'react-dom'
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useLocation,
  useNavigationType,
} from 'react-router-dom'
import { AuthPage } from './auth/AuthPage'
import { ForgotPasswordPage } from './auth/ForgotPasswordPage'
import { ResetPasswordPage } from './auth/ResetPasswordPage'
import { useAuth } from './auth/authStore'
import { PublicOnly, RequireAuth, RequireRole } from './auth/guards'
import { AppLayout } from './layout/AppLayout'
import { CataloguePage } from './pages/CataloguePage'
import { CertificatePage } from './pages/CertificatePage'
import { ChallengePage } from './pages/ChallengePage'
import { DashboardPage } from './pages/DashboardPage'
import { ExamPage } from './pages/ExamPage'
import { LessonPage } from './pages/LessonPage'
import { MyPathsPage } from './pages/MyPathsPage'
import { NodePage } from './pages/NodePage'
import { ProfilePage } from './pages/ProfilePage'
import { RoadmapPage } from './pages/RoadmapPage'
import { AdminPage, NotFoundPage } from './pages'

/** Sections de premier niveau (atteintes par la barre latérale) : ce sont des pairs. */
function isPeer(path: string): boolean {
  return path === '/' || /^\/(catalogue|mes-parcours|profile|admin)$/.test(path)
}

/** Profondeur hiérarchique d'une route, pour décider du sens du push (avant/arrière). */
function routeDepth(path: string): number {
  if (isPeer(path)) return 0
  if (path.startsWith('/roadmaps/')) return 1
  if (path.startsWith('/nodes/') || path.startsWith('/certificate/')) return 2
  if (
    path.startsWith('/lessons/') ||
    path.startsWith('/challenges/') ||
    path.startsWith('/exams/')
  ) {
    return 3
  }
  return 1
}

type Direction = 'fade' | 'forward' | 'back'

/**
 * Transition « façon Apple » :
 * - entre sections pairs (sidebar) -> fondu enchaîné (comme entre onglets) ;
 * - descente liste -> détail -> push latéral (nouvelle depuis la droite) ;
 * - retour (bouton navigateur ou remontée) -> push inversé (depuis la gauche).
 */
function direction(from: string, to: string, navType: string): Direction {
  if (isPeer(from) && isPeer(to)) return 'fade'
  if (navType === 'POP') return 'back'
  const delta = routeDepth(to) - routeDepth(from)
  if (delta < 0) return 'back'
  if (delta > 0) return 'forward'
  return 'fade'
}

type DocWithVT = Document & {
  startViewTransition?: (cb: () => void) => { finished: Promise<void> }
}

/** Routage avec transitions de navigation via la View Transitions API (repli = instantané). */
function AnimatedRoutes() {
  const location = useLocation()
  const navType = useNavigationType()
  // `displayed` = la route réellement rendue ; elle ne change qu'A L'INTERIEUR de la transition,
  // ce qui laisse le navigateur animer l'ancien et le nouvel écran ensemble.
  const [displayed, setDisplayed] = useState(location)
  const displayedRef = useRef(location)
  const fromRef = useRef(location.pathname)
  // On ne dépend QUE de `location` : dépendre de `displayed` ferait ré-entrer l'effet pendant
  // le callback de la transition (flushSync) et l'avorterait (« invalid state »).
  const navTypeRef = useRef(navType)
  navTypeRef.current = navType

  useEffect(() => {
    if (location.key === displayedRef.current.key) return
    const from = fromRef.current
    fromRef.current = location.pathname
    const commit = () => {
      displayedRef.current = location
      setDisplayed(location)
    }

    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    const doc = document as DocWithVT
    if (reduce || typeof doc.startViewTransition !== 'function') {
      commit()
      return
    }

    const dir = direction(from, location.pathname, navTypeRef.current)
    document.documentElement.dataset.vt = dir
    const transition = doc.startViewTransition(() => {
      flushSync(commit)
    })
    void transition.finished.finally(() => {
      if (document.documentElement.dataset.vt === dir) delete document.documentElement.dataset.vt
    })
  }, [location])

  return (
    <Routes location={displayed}>
      <Route
        path="/login"
        element={
          <PublicOnly>
            <AuthPage />
          </PublicOnly>
        }
      />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="catalogue" element={<CataloguePage />} />
          <Route path="mes-parcours" element={<MyPathsPage />} />
          <Route path="roadmaps/:slug" element={<RoadmapPage />} />
          <Route path="nodes/:id" element={<NodePage />} />
          <Route path="lessons/:id" element={<LessonPage />} />
          <Route path="challenges/:id" element={<ChallengePage />} />
          <Route path="certificate/:slug" element={<CertificatePage />} />
          <Route path="exams/:id" element={<ExamPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route element={<RequireRole role="ADMIN" />}>
            <Route path="admin" element={<AdminPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

/** Racine de l'app : restaure la session au démarrage puis expose le routage. */
export function App() {
  const status = useAuth((s) => s.status)
  const bootstrap = useAuth((s) => s.bootstrap)

  useEffect(() => {
    void bootstrap()
  }, [bootstrap])

  if (status === 'idle' || status === 'loading') {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'grid',
          placeItems: 'center',
          color: 'var(--color-text-muted)',
        }}
      >
        Chargement…
      </div>
    )
  }

  return (
    <BrowserRouter>
      <AnimatedRoutes />
    </BrowserRouter>
  )
}
