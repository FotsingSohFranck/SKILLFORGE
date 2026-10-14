/**
 * Identité visuelle par parcours : couleur d'accent + initiales, pour distinguer
 * les parcours d'un coup d'oeil (catalogue, en-tête, tableau de bord).
 */
interface TrackMeta {
  accent: string
  initials: string
}

/* Palette monochrome : graphite doux (quasi-noir) pour toutes les pastilles. */
const STEEL_ACCENT = '#2c2c31'

/** Thème/catégorie d'un parcours (pour le regroupement du catalogue). */
const CATEGORIES: Record<string, string> = {
  backend: 'Développement',
  'java-17-21': 'Langages',
  rust: 'Langages',
  'software-architect': 'Architecture & Design',
  'software-design': 'Architecture & Design',
  docker: 'DevOps & Cloud',
  kubernetes: 'DevOps & Cloud',
  terraform: 'DevOps & Cloud',
  aws: 'DevOps & Cloud',
  'backend-performance': 'Développement',
  'api-security': 'Sécurité',
  'prompt-engineering': 'IA',
  decouverte: 'Découverte',
  demo: 'Découverte',
  kcna: 'Certifications',
  cka: 'Certifications',
  ckad: 'Certifications',
}

export function trackCategory(slug: string, kind: string): string {
  if (kind === 'CERTIFICATION') return 'Certifications'
  return CATEGORIES[slug] ?? 'Autres'
}

function initialsFrom(title: string): string {
  const words = title.replace(/[^\p{L}\p{N}\s]/gu, ' ').trim().split(/\s+/)
  if (words.length === 0) return '?'
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase()
  return (words[0][0] + words[1][0]).toUpperCase()
}

export function trackMeta(_slug: string, title: string): TrackMeta {
  return {
    accent: STEEL_ACCENT,
    initials: initialsFrom(title),
  }
}
