import { trackMeta } from '../trackMeta'

interface TrackBadgeProps {
  slug: string
  title: string
  size?: number
}

/** Pastille colorée avec les initiales du parcours (identité visuelle par parcours). */
export function TrackBadge({ slug, title, size = 40 }: TrackBadgeProps) {
  const { accent, initials } = trackMeta(slug, title)
  return (
    <span
      aria-hidden="true"
      style={{
        flex: 'none',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: size,
        height: size,
        borderRadius: 'var(--radius-md)',
        background: accent,
        color: '#ffffff',
        fontWeight: 700,
        fontSize: size * 0.4,
        letterSpacing: '0.02em',
      }}
    >
      {initials}
    </span>
  )
}
