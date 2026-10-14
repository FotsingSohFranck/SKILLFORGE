import type { HTMLAttributes, ReactNode } from 'react'
import styles from './Badge.module.css'

export type BadgeTone = 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'neutral'

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  tone?: BadgeTone
  children?: ReactNode
}

/** Étiquette de statut compacte, déclinée par tonalité. */
export function Badge({ tone = 'neutral', className, children, ...rest }: BadgeProps) {
  return (
    <span className={[styles.badge, styles[tone], className].filter(Boolean).join(' ')} {...rest}>
      {children}
    </span>
  )
}
