import { useState } from 'react'
import { Button } from './Button'
import styles from './OnboardingTour.module.css'

const SEEN_KEY = 'onboarding-seen-v1'

function alreadySeen(): boolean {
  try {
    return localStorage.getItem(SEEN_KEY) === '1'
  } catch {
    return true // en cas d'indisponibilité, on n'insiste pas
  }
}

function markSeen(): void {
  try {
    localStorage.setItem(SEEN_KEY, '1')
  } catch {
    // sans conséquence
  }
}

const STEPS: { title: string; body: string }[] = [
  {
    title: 'Bienvenue sur SkillForge',
    body: "Tu apprends ici à ton rythme, de débutant à professionnel. Voici l'essentiel en quelques secondes.",
  },
  {
    title: 'Choisis un parcours',
    body: "Dans le Catalogue, inscris-toi à un parcours. Chaque parcours est découpé en nœuds, eux-mêmes en leçons.",
  },
  {
    title: 'Avance et pratique',
    body: "Suis les leçons dans l'ordre conseillé, valide chaque étape par un quiz, et entraîne-toi avec les défis de code.",
  },
  {
    title: 'Recherche rapide',
    body: "Appuie sur Ctrl+K (ou Cmd+K) n'importe quand pour sauter vers un parcours ou un nœud. Ta progression est toujours enregistrée.",
  },
]

/** Petit tour d'accueil affiché une seule fois à la première visite. */
export function OnboardingTour() {
  const [visible, setVisible] = useState(() => !alreadySeen())
  const [step, setStep] = useState(0)

  if (!visible) return null

  function close() {
    markSeen()
    setVisible(false)
  }

  const current = STEPS[step]
  const last = step === STEPS.length - 1

  return (
    <div className={styles.overlay} role="dialog" aria-modal="true" aria-label="Bienvenue">
      <div className={styles.card}>
        <div className={styles.dots} aria-hidden="true">
          {STEPS.map((_, i) => (
            <span key={i} className={i === step ? styles.dotActive : styles.dot} />
          ))}
        </div>
        <h2 className={styles.title}>{current.title}</h2>
        <p className={styles.body}>{current.body}</p>
        <div className={styles.actions}>
          <button type="button" className={styles.skip} onClick={close}>
            Passer
          </button>
          {last ? (
            <Button onClick={close}>Commencer</Button>
          ) : (
            <Button onClick={() => setStep((s) => s + 1)}>Suivant</Button>
          )}
        </div>
      </div>
    </div>
  )
}
