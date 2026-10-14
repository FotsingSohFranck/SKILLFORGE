import { create } from 'zustand'

export type Theme = 'light' | 'dark'

interface ThemeState {
  theme: Theme
  toggle: () => void
  setTheme: (theme: Theme) => void
}

function initialTheme(): Theme {
  const saved = localStorage.getItem('sf-theme')
  if (saved === 'light' || saved === 'dark') return saved
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

function applyTheme(theme: Theme): void {
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('sf-theme', theme)
}

export const useTheme = create<ThemeState>((set, get) => {
  const theme = initialTheme()
  applyTheme(theme)
  return {
    theme,
    toggle: () => {
      const next: Theme = get().theme === 'dark' ? 'light' : 'dark'
      applyTheme(next)
      set({ theme: next })
    },
    setTheme: (theme: Theme) => {
      applyTheme(theme)
      set({ theme })
    },
  }
})
