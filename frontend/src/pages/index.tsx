import { Placeholder } from './Placeholder'

export function AdminPage() {
  return (
    <Placeholder
      title="Administration"
      description="Espace réservé aux administrateurs : gestion du contenu et import (SF-32/40)."
    />
  )
}

export function NotFoundPage() {
  return (
    <Placeholder
      title="404 : Page introuvable"
      description="La page demandée n'existe pas ou a été déplacée."
    />
  )
}
