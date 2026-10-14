import { useState } from 'react'
import styles from './TrackCover.module.css'

/**
 * Bandeau de couverture d'un parcours (banque d'images locale, Pexels).
 * Traité en monochrome pour rester cohérent avec l'identité ; la couleur + un léger zoom
 * se révèlent au survol de la carte (via `.sf-lift:hover [data-cover]`).
 * Si l'image est absente (slug sans visuel), on ne rend rien : la carte garde son en-tête seul.
 */
export function TrackCover({ slug, tall = false }: { slug: string; tall?: boolean }) {
  const [ok, setOk] = useState(true)
  if (!ok) return null
  return (
    <div
      className={[styles.cover, tall ? styles.tall : ''].filter(Boolean).join(' ')}
      data-cover
      aria-hidden="true"
    >
      <img
        src={`/images/tracks/${slug}.jpg`}
        alt=""
        loading="lazy"
        onError={() => setOk(false)}
      />
    </div>
  )
}
