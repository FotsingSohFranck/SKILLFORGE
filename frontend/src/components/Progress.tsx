import styles from './Progress.module.css'

interface ProgressProps {
  value: number
  label?: string
  showPercent?: boolean
}

/** Barre de progression accessible (0–100). */
export function Progress({ value, label, showPercent = true }: ProgressProps) {
  const clamped = Math.max(0, Math.min(100, value))
  const rounded = Math.round(clamped)
  return (
    <div className={styles.wrap}>
      {(label || showPercent) && (
        <div className={styles.head}>
          {label && <span>{label}</span>}
          {showPercent && <span className={styles.percent}>{rounded}%</span>}
        </div>
      )}
      <div
        className={styles.track}
        role="progressbar"
        aria-valuenow={rounded}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label={label ?? 'Progression'}
      >
        <div className={styles.bar} style={{ width: `${clamped}%` }} />
      </div>
    </div>
  )
}
