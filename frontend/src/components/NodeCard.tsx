import { Badge, type BadgeTone } from './Badge'
import { Progress } from './Progress'
import styles from './NodeCard.module.css'

export type NodeStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'
export type NodeImportance = 'CORE' | 'RECOMMENDED' | 'OPTIONAL'

interface NodeCardProps {
  title: string
  status: NodeStatus
  percent?: number
  importance?: NodeImportance
  onClick?: () => void
}

const STATUS: Record<NodeStatus, { label: string; tone: BadgeTone }> = {
  NOT_STARTED: { label: 'Non commencé', tone: 'neutral' },
  IN_PROGRESS: { label: 'En cours', tone: 'warning' },
  COMPLETED: { label: 'Terminé', tone: 'success' },
}

/** Carte de nœud d'une roadmap : titre, statut visuel et progression. */
export function NodeCard({ title, status, percent = 0, importance, onClick }: NodeCardProps) {
  const meta = STATUS[status]
  return (
    <button
      type="button"
      className={[styles.card, styles[status.toLowerCase()], 'sf-pressable'].join(' ')}
      onClick={onClick}
      aria-label={`${title} : ${meta.label}`}
    >
      <div className={styles.top}>
        <span className={styles.title}>{title}</span>
        <Badge tone={meta.tone}>{meta.label}</Badge>
      </div>
      {status !== 'NOT_STARTED' && <Progress value={percent} showPercent />}
      {importance && (
        <div className={styles.footer}>
          <Badge tone={importance === 'CORE' ? 'primary' : 'neutral'}>{importance}</Badge>
        </div>
      )}
    </button>
  )
}
