import type { ReactNode } from 'react'
import { Card } from '../components/Card'
import styles from './Placeholder.module.css'

interface PlaceholderProps {
  title: string
  description: string
  children?: ReactNode
}

/** Gabarit temporaire pour les pages privées non encore implémentées (SF-32/33). */
export function Placeholder({ title, description, children }: PlaceholderProps) {
  return (
    <div className={styles.page}>
      <h1 className={styles.title}>{title}</h1>
      <Card>
        <p className={styles.desc}>{description}</p>
        {children}
      </Card>
    </div>
  )
}
