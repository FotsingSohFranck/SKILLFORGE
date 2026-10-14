import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Card } from '../components/Card'
import { ThemeToggle } from '../components/ThemeToggle'
import { LoginForm } from './LoginForm'
import { RegisterForm } from './RegisterForm'
import styles from './AuthPage.module.css'

/** Page publique d'authentification : bascule connexion / inscription. */
export function AuthPage() {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  return (
    <div className={styles.page}>
      <div className={styles.topbar}>
        <ThemeToggle />
      </div>
      <div className={styles.center}>
        <div className={styles.brand}>
          <h1 className={styles.title}>SkillForge</h1>
          <p className={styles.tagline}>De débutant à professionnel, à ton rythme.</p>
        </div>
        <Card title={mode === 'login' ? 'Connexion' : 'Créer un compte'} className={styles.card}>
          {mode === 'login' ? <LoginForm /> : <RegisterForm />}
          {mode === 'login' && (
            <div className={styles.switch}>
              <Link to="/forgot-password" className={styles.link}>
                Mot de passe oublié ?
              </Link>
            </div>
          )}
          <div className={styles.switch}>
            {mode === 'login' ? (
              <button type="button" className={styles.link} onClick={() => setMode('register')}>
                Pas encore de compte ? S'inscrire
              </button>
            ) : (
              <button type="button" className={styles.link} onClick={() => setMode('login')}>
                Déjà un compte ? Se connecter
              </button>
            )}
          </div>
        </Card>
      </div>
    </div>
  )
}
