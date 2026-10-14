import { useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { Callout } from '../components/Callout'
import { ApiError } from '../api/types'
import { useAuth } from './authStore'

/** Formulaire de connexion. */
export function LoginForm() {
  const login = useAuth((s) => s.login)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(email.trim(), password)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Connexion impossible.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      {error && <Callout tone="danger" role="alert">{error}</Callout>}
      <Input
        label="Adresse e-mail"
        type="email"
        autoComplete="email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="vous@exemple.dev"
      />
      <Input
        label="Mot de passe"
        type="password"
        autoComplete="current-password"
        required
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
      <Button type="submit" variant="primary" disabled={loading}>
        {loading ? 'Connexion…' : 'Se connecter'}
      </Button>
    </form>
  )
}
