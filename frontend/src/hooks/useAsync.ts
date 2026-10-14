import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../api/types'

export interface AsyncState<T> {
  data: T | null
  loading: boolean
  error: string | null
  reload: () => void
}

function messageOf(err: unknown): string {
  if (err instanceof ApiError) return err.message
  if (err instanceof Error) return err.message
  return 'Une erreur est survenue.'
}

/**
 * Exécute un fetcher au montage (et à chaque changement de `deps`), en exposant
 * l'état chargement / erreur / données et un `reload` manuel.
 */
export function useAsync<T>(fetcher: () => Promise<T>, deps: unknown[] = []): AsyncState<T> {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tick, setTick] = useState(0)

  const reload = useCallback(() => setTick((t) => t + 1), [])

  useEffect(() => {
    let active = true
    setLoading(true)
    setError(null)
    fetcher()
      .then((result) => {
        if (active) setData(result)
      })
      .catch((err) => {
        if (active) setError(messageOf(err))
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tick, ...deps])

  return { data, loading, error, reload }
}
