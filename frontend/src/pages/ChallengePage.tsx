import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getChallenge, submitChallenge } from '../api/evaluation'
import { ApiError, type Challenge, type SubmissionResult } from '../api/types'
import { AsyncSection } from '../components/AsyncSection'
import { Badge } from '../components/Badge'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { CodeBlock } from '../components/CodeBlock'
import { CodeEditor } from '../components/CodeEditor'
import { IconCheck, IconCross } from '../components/icons'
import { deEmDash } from '../format'
import { useToasts } from '../components/toast/toastStore'
import { useAsync } from '../hooks/useAsync'
import styles from './ChallengePage.module.css'

const draftKey = (id: string) => `challenge-code-${id}`

function loadDraft(id: string): string | null {
  try {
    return localStorage.getItem(draftKey(id))
  } catch {
    return null
  }
}

function saveDraft(id: string, code: string): void {
  try {
    localStorage.setItem(draftKey(id), code)
  } catch {
    // stockage indisponible (mode privé, quota) : sans conséquence
  }
}

export function ChallengePage() {
  const { id = '' } = useParams()
  const push = useToasts((s) => s.push)
  const state = useAsync(() => getChallenge(id), [id])
  const [code, setCode] = useState('')
  const [result, setResult] = useState<SubmissionResult | null>(null)
  const [submitting, setSubmitting] = useState(false)

  // Restaure un brouillon sauvegardé, sinon pré-remplit avec le squelette.
  useEffect(() => {
    if (!state.data) return
    const saved = loadDraft(id)
    setCode(saved ?? state.data.skeleton ?? '')
  }, [state.data, id])

  // Autosave : le code n'est plus perdu à la navigation.
  useEffect(() => {
    if (state.data) saveDraft(id, code)
  }, [code, id, state.data])

  async function submit() {
    setSubmitting(true)
    try {
      setResult(await submitChallenge(id, code))
    } catch (err) {
      push('danger', err instanceof ApiError ? err.message : 'Soumission impossible.')
    } finally {
      setSubmitting(false)
    }
  }

  function reset(challenge: Challenge) {
    setCode(challenge.skeleton ?? '')
    setResult(null)
  }

  return (
    <div className={styles.page}>
      <AsyncSection state={state}>
        {(challenge) => (
          <>
            <header className={styles.header}>
              <h1 className={styles.title}>{challenge.title}</h1>
              {challenge.language && <Badge tone="secondary">{challenge.language}</Badge>}
            </header>

            <Card title="Énoncé">
              <p className={styles.statement}>{deEmDash(challenge.statement)}</p>
            </Card>

            <section className={styles.editor}>
              <span className={styles.label}>Votre solution</span>
              <CodeEditor
                value={code}
                onChange={setCode}
                language={challenge.language ?? 'java'}
              />
              <div className={styles.actions}>
                <Button onClick={() => void submit()} disabled={submitting || !code.trim()}>
                  {submitting ? 'Validation…' : 'Soumettre'}
                </Button>
                <Button variant="ghost" onClick={() => reset(challenge)}>
                  Réinitialiser
                </Button>
              </div>
            </section>

            {result && <ResultView result={result} language={challenge.language} />}
          </>
        )}
      </AsyncSection>
    </div>
  )
}

function ResultView({ result, language }: { result: SubmissionResult; language: string | null }) {
  return (
    <section className={styles.result}>
      <div className={styles.resultHead}>
        <Badge tone={result.passed ? 'success' : 'danger'}>
          {result.passed ? 'Réussi' : 'À retravailler'}
        </Badge>
      </div>

      <p className={styles.resultNote}>
        Vérification des éléments clés attendus dans votre solution (pas une exécution du code).
      </p>

      <div className={styles.keypoints}>
        {result.matched.map((kp) => (
          <div key={`m-${kp}`} className={styles.matched}>
            <IconCheck size={15} /> {kp}
          </div>
        ))}
        {result.missing.map((kp) => (
          <div key={`x-${kp}`} className={styles.missing}>
            <IconCross size={15} /> {kp}
          </div>
        ))}
      </div>

      <details className={styles.solution}>
        <summary>Voir une solution de référence</summary>
        <CodeBlock code={result.solution} language={language ?? 'java'} />
      </details>
    </section>
  )
}
