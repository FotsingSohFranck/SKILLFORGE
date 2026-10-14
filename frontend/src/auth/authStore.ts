import { create } from 'zustand'
import { apiFetch, rawRequest } from '../api/client'
import type { Tokens, UserProfile } from '../api/types'

const REFRESH_KEY = 'sf-refresh'

function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_KEY)
}
function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_KEY, token)
}
function clearRefreshToken(): void {
  localStorage.removeItem(REFRESH_KEY)
}

export type AuthStatus = 'idle' | 'loading' | 'authenticated' | 'anonymous'

interface AuthState {
  accessToken: string | null
  user: UserProfile | null
  status: AuthStatus
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, displayName: string) => Promise<void>
  logout: () => Promise<void>
  refresh: () => Promise<boolean>
  loadMe: () => Promise<void>
  bootstrap: () => Promise<void>
  hasRole: (role: string) => boolean
}

export const useAuth = create<AuthState>((set, get) => ({
  accessToken: null,
  user: null,
  status: 'idle',

  login: async (email, password) => {
    const tokens = await rawRequest<Tokens>('POST', '/auth/login', { email, password })
    setRefreshToken(tokens.refreshToken)
    set({ accessToken: tokens.accessToken })
    await get().loadMe()
  },

  register: async (email, password, displayName) => {
    await rawRequest<UserProfile>('POST', '/auth/register', { email, password, displayName })
    await get().login(email, password)
  },

  refresh: async () => {
    const token = getRefreshToken()
    if (!token) {
      set({ accessToken: null, user: null, status: 'anonymous' })
      return false
    }
    try {
      const tokens = await rawRequest<Tokens>('POST', '/auth/refresh', { refreshToken: token })
      setRefreshToken(tokens.refreshToken)
      set({ accessToken: tokens.accessToken })
      return true
    } catch {
      clearRefreshToken()
      set({ accessToken: null, user: null, status: 'anonymous' })
      return false
    }
  },

  loadMe: async () => {
    const me = await apiFetch<UserProfile>('GET', '/auth/me')
    set({ user: me, status: 'authenticated' })
  },

  logout: async () => {
    const token = getRefreshToken()
    if (token) {
      try {
        await rawRequest('POST', '/auth/logout', { refreshToken: token })
      } catch {
        // révocation best-effort
      }
    }
    clearRefreshToken()
    set({ accessToken: null, user: null, status: 'anonymous' })
  },

  bootstrap: async () => {
    if (!getRefreshToken()) {
      set({ status: 'anonymous' })
      return
    }
    set({ status: 'loading' })
    const ok = await get().refresh()
    if (!ok) {
      set({ status: 'anonymous' })
      return
    }
    try {
      await get().loadMe()
    } catch {
      set({ status: 'anonymous' })
    }
  },

  hasRole: (role) => get().user?.roles.includes(role) ?? false,
}))
