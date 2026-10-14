import type { ComponentType, ReactNode } from 'react'
import { IconBan, IconCheckCircle, IconInfo, IconWarning } from './icons'
import styles from './Callout.module.css'

export type CalloutTone = 'info' | 'success' | 'warning' | 'danger'

interface CalloutProps {
  tone?: CalloutTone
  title?: string
  /** Rôle ARIA (`note` par défaut ; `alert` pour un message d'erreur à annoncer). */
  role?: string
  children?: ReactNode
}

const ICONS: Record<CalloutTone, ComponentType<{ size?: number }>> = {
  info: IconInfo,
  success: IconCheckCircle,
  warning: IconWarning,
  danger: IconBan,
}

/** Encadré d'information typé (info / succès / avertissement / danger). */
export function Callout({ tone = 'info', title, role = 'note', children }: CalloutProps) {
  const Icon = ICONS[tone]
  return (
    <div className={[styles.callout, styles[tone]].join(' ')} role={role}>
      <span className={styles.icon} aria-hidden="true"><Icon size={18} /></span>
      <div>
        {title && <div className={styles.title}>{title}</div>}
        <div className={styles.content}>{children}</div>
      </div>
    </div>
  )
}
