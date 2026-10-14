interface ProgressRingProps {
  value: number
  size?: number
  stroke?: number
  color?: string
}

/** Anneau de progression (0..100) avec le pourcentage au centre. */
export function ProgressRing({ value, size = 64, stroke = 6, color }: ProgressRingProps) {
  const v = Math.max(0, Math.min(100, value))
  const r = (size - stroke) / 2
  const c = 2 * Math.PI * r
  const offset = c * (1 - v / 100)
  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${size} ${size}`}
      role="img"
      aria-label={`Progression ${Math.round(v)} %`}
    >
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        fill="none"
        stroke="var(--color-surface-2)"
        strokeWidth={stroke}
      />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        fill="none"
        stroke={color ?? 'var(--color-primary)'}
        strokeWidth={stroke}
        strokeLinecap="round"
        strokeDasharray={c}
        strokeDashoffset={offset}
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
        style={{ transition: 'stroke-dashoffset 0.5s ease' }}
      />
      <text
        x="50%"
        y="50%"
        dominantBaseline="central"
        textAnchor="middle"
        fontSize={size * 0.26}
        fontWeight={700}
        fill="var(--color-text)"
      >
        {Math.round(v)}%
      </text>
    </svg>
  )
}
