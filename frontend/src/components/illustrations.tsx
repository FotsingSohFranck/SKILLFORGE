/**
 * Illustrations SVG inline (légères, hors-ligne, compatibles CSP).
 * Couleurs pilotées par les variables de thème pour s'adapter clair/sombre.
 */

/** Scène « apprentissage » pour les états vides (pile de cartes + progression + étoiles). */
export function IllustrationEmpty({ width = 220 }: { width?: number }) {
  return (
    <svg
      width={width}
      viewBox="0 0 240 170"
      role="img"
      aria-label="Commence à apprendre"
      style={{ maxWidth: '100%', height: 'auto' }}
    >
      <ellipse cx="120" cy="150" rx="92" ry="12" fill="var(--color-surface-2)" />
      {/* cartes empilées */}
      <rect x="54" y="92" width="132" height="42" rx="10" fill="var(--color-surface-2)" />
      <rect x="46" y="70" width="148" height="46" rx="12" fill="color-mix(in srgb, var(--color-secondary) 22%, var(--color-surface))" stroke="var(--color-border)" />
      <rect x="38" y="44" width="164" height="52" rx="14" fill="var(--color-surface)" stroke="var(--color-primary)" strokeWidth="2" />
      {/* barre de progression sur la carte du dessus */}
      <rect x="54" y="78" width="104" height="8" rx="4" fill="var(--color-surface-2)" />
      <rect x="54" y="78" width="62" height="8" rx="4" fill="var(--color-primary)" />
      <circle cx="176" cy="70" r="10" fill="color-mix(in srgb, var(--color-primary) 18%, var(--color-surface))" />
      <path d="M172 70l3 3 5-6" stroke="var(--color-primary)" strokeWidth="2.4" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      {/* étoiles / éclats */}
      <path d="M206 30l2 5 5 2-5 2-2 5-2-5-5-2 5-2 2-5Z" fill="var(--color-secondary)" />
      <path d="M28 26l1.6 4 4 1.6-4 1.6L28 38l-1.6-4-4-1.6 4-1.6L28 26Z" fill="var(--color-primary)" />
    </svg>
  )
}

/** Décor géométrique discret pour l'arrière-plan d'un hero. */
export function HeroDecoration() {
  return (
    <svg
      viewBox="0 0 200 120"
      role="presentation"
      aria-hidden="true"
      preserveAspectRatio="xMaxYMid slice"
      style={{ position: 'absolute', right: 0, top: 0, height: '100%', opacity: 0.5, pointerEvents: 'none' }}
    >
      <circle cx="170" cy="30" r="46" fill="color-mix(in srgb, var(--color-primary) 16%, transparent)" />
      <circle cx="120" cy="96" r="30" fill="color-mix(in srgb, var(--color-secondary) 16%, transparent)" />
      <path d="M150 70l3 7 7 3-7 3-3 7-3-7-7-3 7-3 3-7Z" fill="color-mix(in srgb, var(--color-primary) 40%, transparent)" />
    </svg>
  )
}
