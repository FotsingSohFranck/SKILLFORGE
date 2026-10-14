import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { ThemeToggle } from '../components/ThemeToggle'
import { LoginForm } from './LoginForm'
import { RegisterForm } from './RegisterForm'
import styles from './AuthPage.module.css'

/** Page publique d'authentification : panneau formulaire + panneau image (hero). */
export function AuthPage() {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  return (
    <div className={styles.page}>
      <div className={styles.formPanel}>
        <div className={styles.topbar}>
          <ThemeToggle />
        </div>
        <div className={`${styles.center} sf-stagger`}>
          <div className={styles.brand}>
            <span className={styles.logo} aria-hidden="true">
              <svg viewBox="0 0 24 24" width="42" height="42" fill="none">
                <path d="M12 2 3 7v10l9 5 9-5V7l-9-5Z" fill="var(--color-primary)" />
                <path d="M12 7 7 9.5v5L12 17l5-2.5v-5L12 7Z" fill="var(--color-on-primary)" opacity="0.9" />
              </svg>
            </span>
            <h1 className={styles.title}>SkillForge</h1>
            <p className={styles.tagline}>De débutant à professionnel, à ton rythme.</p>
          </div>

          <Card title={mode === 'login' ? 'Connexion' : 'Créer un compte'} className={styles.card}>
            {mode === 'login' ? <LoginForm /> : <RegisterForm />}

            {mode === 'login' && (
              <div className={styles.forgot}>
                <Link to="/forgot-password" className={styles.link}>
                  Mot de passe oublié ?
                </Link>
              </div>
            )}

            <div className={styles.divider}>
              <span>{mode === 'login' ? 'Nouveau sur SkillForge ?' : 'Déjà inscrit ?'}</span>
            </div>
            <Button
              type="button"
              variant="ghost"
              className={styles.switchBtn}
              onClick={() => setMode(mode === 'login' ? 'register' : 'login')}
            >
              {mode === 'login' ? 'Créer un compte' : 'Se connecter'}
            </Button>
          </Card>
        </div>
      </div>

      <aside className={styles.imagePanel} aria-hidden="true">
        <img className={styles.imagePanelImg} src="/images/hero-home.jpg" alt="" />
        <div className={styles.imagePanelScrim} />
        <p className={styles.imagePanelQuote}>
          Apprends le backend pas à pas, du premier concept jusqu'au niveau pro.
        </p>
      </aside>
    </div>
  )
}
