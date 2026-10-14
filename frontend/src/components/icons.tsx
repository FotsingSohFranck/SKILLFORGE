/**
 * Jeu d'icônes SVG inline (aucune dépendance externe, compatibles CSP).
 * Toutes héritent de la couleur du texte (`currentColor`) et se dimensionnent via `size`.
 * On remplace ainsi les emojis, aussi bien dans l'UI que dans le contenu des leçons.
 */
import type { ReactNode, SVGProps } from 'react'

interface IconProps extends SVGProps<SVGSVGElement> {
  size?: number
  title?: string
  /** Variante pleine (filled) : signal d'etat actif/selectionne, facon SF Symbols. */
  filled?: boolean
}

function base({ size = 16, title, filled: _filled, children, ...rest }: IconProps & { children: ReactNode }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.75}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden={title ? undefined : true}
      role={title ? 'img' : undefined}
      focusable="false"
      style={{ display: 'inline-block', verticalAlign: '-0.125em', flex: 'none' }}
      {...rest}
    >
      {title ? <title>{title}</title> : null}
      {children}
    </svg>
  )
}

/** Variante pleine : silhouette remplie (fill), sans trait. Pour l'etat actif. */
function solid({ size = 16, title, filled: _filled, children, ...rest }: IconProps & { children: ReactNode }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="currentColor"
      stroke="none"
      aria-hidden={title ? undefined : true}
      role={title ? 'img' : undefined}
      focusable="false"
      style={{ display: 'inline-block', verticalAlign: '-0.125em', flex: 'none' }}
      {...rest}
    >
      {title ? <title>{title}</title> : null}
      {children}
    </svg>
  )
}

/** Repère « astuce / à noter » (remplace 👉). */
export const IconInsight = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 8v4" />
      <path d="M12 16h.01" />
    </>
  ) })

/** Triangle d'avertissement (remplace ⚠️). */
export const IconWarning = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <path d="M10.3 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.7 3.86a2 2 0 0 0-3.4 0Z" />
      <path d="M12 9v4" />
      <path d="M12 17h.01" />
    </>
  ) })

/** Coche (remplace ✅ ✓). */
export const IconCheck = (p: IconProps) => base({ ...p, children: <path d="M20 6 9 17l-5-5" /> })

/** Coche dans un cercle (état « terminé »). */
export const IconCheckCircle = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="m8.5 12 2.5 2.5L16 9" />
    </>
  ) })

/** Croix (remplace ❌ ✗ ✕). */
export const IconCross = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <path d="M18 6 6 18" />
      <path d="m6 6 12 12" />
    </>
  ) })

/** Cercle vide (état « non commencé »). */
export const IconCircle = (p: IconProps) => base({ ...p, children: <circle cx="12" cy="12" r="9" /> })

/** Interdit (remplace ⛔). */
export const IconBan = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="m5.6 5.6 12.8 12.8" />
    </>
  ) })

/** Info (remplace ℹ️). */
export const IconInfo = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v5" />
      <path d="M12 8h.01" />
    </>
  ) })

/** Flamme / série (remplace 🔥). */
export const IconFlame = (p: IconProps) =>
  base({ ...p, children: (
    <path d="M12 2s5 4.5 5 9a5 5 0 0 1-10 0c0-1.6.7-2.9 1.5-3.8C8.9 8 9 9.4 10 10c0-2.5 1-5 2-8Z" />
  ) })

/** Éclair / défi (remplace ⚡). */
export const IconBolt = (p: IconProps) =>
  base({ ...p, children: <path d="M13 2 4 14h7l-1 8 9-12h-7l1-8Z" /> })

/** Chronomètre (remplace ⏱️). */
export const IconClock = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <circle cx="12" cy="13" r="8" />
      <path d="M12 9v4l2.5 2" />
      <path d="M9 2h6" />
    </>
  ) })

/** Soleil (thème clair). */
export const IconSun = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
    </>
  ) })

/** Lune (thème sombre). */
export const IconMoon = (p: IconProps) =>
  base({ ...p, children: <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" /> })

/** Étoile pleine (favori actif). */
export const IconStar = (p: IconProps) =>
  base({ ...p, fill: 'currentColor', children: (
    <path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8-5.2-2.7-5.2 2.7 1-5.8L3.5 9.7l5.9-.9L12 3.5Z" />
  ) })

/** Étoile vide (favori inactif). */
export const IconStarOutline = (p: IconProps) =>
  base({ ...p, children: (
    <path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8-5.2-2.7-5.2 2.7 1-5.8L3.5 9.7l5.9-.9L12 3.5Z" />
  ) })

/** Médaille / badge (remplace 🏅). Trait épuré, cohérent avec le motif « coche ». */
export const IconMedal = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <path d="M8 2h8l-2.5 7h-3L8 2Z" />
      <circle cx="12" cy="15" r="6" />
      <path d="m9.5 15 1.8 1.8 3.2-3.3" />
    </>
  ) })

/** Accueil. Variante pleine à l'état actif. */
export const IconHome = ({ filled, ...p }: IconProps) =>
  filled
    ? solid({ ...p, children: (
        <path d="M11.47 3.84a.75.75 0 0 1 1.06 0l8.69 8.69a.75.75 0 0 1-.53 1.28H19.5v6.44a.75.75 0 0 1-.75.75H15a.75.75 0 0 1-.75-.75v-4.5h-4.5v4.5a.75.75 0 0 1-.75.75H5.25a.75.75 0 0 1-.75-.75V13.8H3.31a.75.75 0 0 1-.53-1.28l8.69-8.69Z" />
      ) })
    : base({ ...p, children: (
        <>
          <path d="M3 10.5 12 3l9 7.5" />
          <path d="M5 9.5V21h14V9.5" />
          <path d="M9.5 21v-6h5v6" />
        </>
      ) })

/** Catalogue (livre / parcours). Variante pleine à l'état actif. */
export const IconBook = ({ filled, ...p }: IconProps) =>
  filled
    ? solid({ ...p, children: (
        <path d="M6.5 2A2.5 2.5 0 0 0 4 4.5v15A2.5 2.5 0 0 0 6.5 22H20V2H6.5Zm0 16H18v2H6.5a.5.5 0 0 1 0-1V18Z" />
      ) })
    : base({ ...p, children: (
        <>
          <path d="M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2V5Z" />
          <path d="M4 19a2 2 0 0 1 2-2h13" />
        </>
      ) })

/** Profil (utilisateur). Variante pleine à l'état actif. */
export const IconUser = ({ filled, ...p }: IconProps) =>
  filled
    ? solid({ ...p, children: (
        <>
          <circle cx="12" cy="8" r="4.2" />
          <path d="M4 20.5C4 16.4 7.6 14 12 14s8 2.4 8 6.5a.5.5 0 0 1-.5.5H4.5a.5.5 0 0 1-.5-.5Z" />
        </>
      ) })
    : base({ ...p, children: (
        <>
          <circle cx="12" cy="8" r="4" />
          <path d="M4 21c0-4 3.6-6.5 8-6.5s8 2.5 8 6.5" />
        </>
      ) })

/** Administration (bouclier). Variante pleine à l'état actif. */
export const IconShield = ({ filled, ...p }: IconProps) =>
  filled
    ? solid({ ...p, children: (
        <path d="M12 2.6 4.8 5.6a1 1 0 0 0-.6.92V11c0 4.9 3.3 8.9 7.5 11 .2.1.4.1.6 0C16.5 19.9 19.8 15.9 19.8 11V6.52a1 1 0 0 0-.6-.92L12 2.6Z" />
      ) })
    : base({ ...p, children: (
        <>
          <path d="M12 3 5 6v5c0 4.4 3 8 7 10 4-2 7-5.6 7-10V6l-7-3Z" />
          <path d="m9 12 2 2 4-4" />
        </>
      ) })

/** Recherche (loupe). */
export const IconSearch = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </>
  ) })

/** Déconnexion. */
export const IconLogout = (p: IconProps) =>
  base({ ...p, children: (
    <>
      <path d="M15 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3" />
      <path d="M10 17 5 12l5-5" />
      <path d="M5 12h12" />
    </>
  ) })
