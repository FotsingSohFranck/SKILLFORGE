import type { ReactNode } from 'react'
import type { AsyncState } from '../hooks/useAsync'
import { Button } from './Button'
import { Callout } from './Callout'

interface AsyncSectionProps<T> {
  state: AsyncState<T>
  children: (data: T) => ReactNode
  empty?: ReactNode
}

function isEmpty(data: unknown): boolean {
  if (data == null) return true
  if (Array.isArray(data)) return data.length === 0
  return false
}

/** Rend les états chargement / erreur / vide d'un `useAsync`, sinon délègue au render-prop. */
export function AsyncSection<T>({ state, children, empty }: AsyncSectionProps<T>): ReactNode {
  if (state.loading && state.data == null) {
    return (
      <p role="status" aria-live="polite" style={{ color: 'var(--color-text-muted)' }}>
        Chargement…
      </p>
    )
  }
  if (state.error) {
    return (
      <Callout tone="danger" title="Erreur de chargement" role="alert">
        <p style={{ margin: '0 0 var(--space-2)' }}>{state.error}</p>
        <Button variant="secondary" size="sm" onClick={state.reload}>
          Réessayer
        </Button>
      </Callout>
    )
  }
  if (isEmpty(state.data)) {
    return (
      <>{empty ?? <p style={{ color: 'var(--color-text-muted)' }}>Rien à afficher pour le moment.</p>}</>
    )
  }
  return <>{children(state.data as T)}</>
}
