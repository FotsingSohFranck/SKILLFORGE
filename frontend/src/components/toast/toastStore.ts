import { create } from 'zustand'

export type ToastTone = 'info' | 'success' | 'danger'

export interface Toast {
  id: number
  tone: ToastTone
  message: string
}

interface ToastState {
  toasts: Toast[]
  push: (tone: ToastTone, message: string) => void
  dismiss: (id: number) => void
}

let nextId = 1

export const useToasts = create<ToastState>((set) => ({
  toasts: [],
  push: (tone, message) => {
    const id = nextId++
    set((s) => ({ toasts: [...s.toasts, { id, tone, message }] }))
    setTimeout(() => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })), 3500)
  },
  dismiss: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
}))
