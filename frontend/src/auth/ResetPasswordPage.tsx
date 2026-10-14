import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../api/auth'
import { ApiError } from '../api/types'
import { Button } from '../components/Button'
import { Callout } from '../components/Callout'
import { Card } from '../components/Card'
import { Input } from '../components/Input'
import { ThemeToggle } from '../components/ThemeToggle'
import styles from './AuthPage.module.css'

/** Page publique : définition d'un nouveau mot de passe à partir du jeton reçu par e-mail. */
export function ResetPasswordPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const token = params.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    if (password.length < 8) {
      setError('Le mot de passe doit faire au moins 8 caractères.')
      return
    }
    if (password !== confirm) {
      setError('Les deux mots de passe ne correspondent pas.')
      return
    }
    setLoading(true)
    try {
      await resetPassword(token, password)
      setDone(true)
      setTimeout(() => navigate('/login'), 2500)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Réinitialisation impossible.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.topbar}>
        <ThemeToggle />
      </div>
      <div className={styles.center}>
        <div className={styles.brand}>
          <h1 className={styles.title}>SkillForge</h1>
          <p className={styles.tagline}>Nouveau mot de passe.</p>
        </div>
        <Card title="Définir un nouveau mot de passe" className={styles.card}>
          {!token ? (
            <>
              <Callout tone="danger">
                Lien invalide : aucun jeton de réinitialisation n'a été fourni.
              </Callout>
              <div className={styles.switch}>
                <Link to="/forgot-password" className={styles.link}>
                  Demander un nouveau lien
                </Link>
              </div>
            </>
          ) : done ? (
            <>
              <Callout tone="success">
                Mot de passe mis à jour. Tu vas être redirigé vers la connexion.
              </Callout>
              <div className={styles.switch}>
                <Link to="/login" className={styles.link}>
                  Se connecter maintenant
                </Link>
              </div>
            </>
          ) : (
            <form
              onSubmit={submit}
              style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}
            >
              {error && (
                <Callout tone="danger" role="alert">
                  {error}
                </Callout>
              )}
              <Input
                label="Nouveau mot de passe"
                type="password"
                autoComplete="new-password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <Input
                label="Confirme le mot de passe"
                type="password"
                autoComplete="new-password"
                required
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
              />
              <Button type="submit" variant="primary" disabled={loading}>
                {loading ? 'Mise à jour…' : 'Définir le mot de passe'}
              </Button>
            </form>
          )}
        </Card>
      </div>
    </div>
  )
}
