import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './authStore'

/** N'affiche les routes privées que si l'utilisateur est authentifié. */
export function RequireAuth() {
  const status = useAuth((s) => s.status)
  if (status === 'anonymous') {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}

/** Restreint l'accès à un rôle donné (sinon redirige vers l'accueil). */
export function RequireRole({ role }: { role: string }) {
  const hasRole = useAuth((s) => s.hasRole(role))
  if (!hasRole) {
    return <Navigate to="/" replace />
  }
  return <Outlet />
}

/** Redirige vers l'accueil si déjà authentifié (pages publiques login/register). */
export function PublicOnly({ children }: { children: React.ReactNode }) {
  const status = useAuth((s) => s.status)
  if (status === 'authenticated') {
    return <Navigate to="/" replace />
  }
  return <>{children}</>
}
