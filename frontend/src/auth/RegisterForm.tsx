import { useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { Callout } from '../components/Callout'
import { ApiError } from '../api/types'
import { useAuth } from './authStore'

/** Formulaire d'inscription (crée le compte puis connecte). */
export function RegisterForm() {
  const register = useAuth((s) => s.register)
  const [email, setEmail] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await register(email.trim(), password, displayName.trim())
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Inscription impossible.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      {error && <Callout tone="danger" role="alert">{error}</Callout>}
      <Input
        label="Nom d'affichage"
        required
        value={displayName}
        onChange={(e) => setDisplayName(e.target.value)}
        placeholder="Votre nom"
      />
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
        autoComplete="new-password"
        required
        minLength={8}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
      <Button type="submit" variant="primary" disabled={loading}>
        {loading ? 'Création…' : 'Créer mon compte'}
      </Button>
    </form>
  )
}
