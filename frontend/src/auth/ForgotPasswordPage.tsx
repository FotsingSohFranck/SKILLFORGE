import { useState } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../api/auth'
import { Button } from '../components/Button'
import { Callout } from '../components/Callout'
import { Card } from '../components/Card'
import { Input } from '../components/Input'
import { ThemeToggle } from '../components/ThemeToggle'
import styles from './AuthPage.module.css'

/** Page publique : demande d'un lien de réinitialisation (réponse neutre). */
export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [loading, setLoading] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      await forgotPassword(email.trim())
    } catch {
      // Réponse volontairement neutre : on n'indique jamais si le compte existe.
    } finally {
      setLoading(false)
      setSent(true)
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
          <p className={styles.tagline}>Réinitialisation du mot de passe.</p>
        </div>
        <Card title="Mot de passe oublié" className={styles.card}>
          {sent ? (
            <>
              <Callout tone="info">
                Si un compte existe pour cette adresse, un e-mail contenant un lien de
                réinitialisation vient d'être envoyé. Le lien est valable un temps limité.
              </Callout>
              <div className={styles.switch}>
                <Link to="/login" className={styles.link}>
                  Retour à la connexion
                </Link>
              </div>
            </>
          ) : (
            <form
              onSubmit={submit}
              style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}
            >
              <p style={{ margin: 0, color: 'var(--color-text-muted)' }}>
                Saisis l'adresse e-mail de ton compte. Nous t'enverrons un lien pour définir un
                nouveau mot de passe.
              </p>
              <Input
                label="Adresse e-mail"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="vous@exemple.dev"
              />
              <Button type="submit" variant="primary" disabled={loading}>
                {loading ? 'Envoi…' : 'Envoyer le lien'}
              </Button>
              <div className={styles.switch}>
                <Link to="/login" className={styles.link}>
                  Retour à la connexion
                </Link>
              </div>
            </form>
          )}
        </Card>
      </div>
    </div>
  )
}
