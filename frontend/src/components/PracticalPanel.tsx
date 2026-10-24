import { useId, useState } from 'react'
import { getPractical, submitPractical, type LabPractical, type LabVerdict } from '../api/lab'
import { ApiError } from '../api/types'
import { useToasts } from './toast/toastStore'
import { Badge } from './Badge'
import { Button } from './Button'
import { Card } from './Card'
import { RichText } from './RichText'
import styles from './PracticalPanel.module.css'

interface Props {
  nodeId: string
  practical: LabPractical
}

/** Épreuve pratique d'un nœud : consigne, éditeur de code, validation et verdict. */
export function PracticalPanel({ nodeId, practical }: Props) {
  const push = useToasts((s) => s.push)
  const fileId = useId()
  const codeId = useId()
  const [path, setPath] = useState(practical.expectedPath ?? 'solution.txt')
  const [code, setCode] = useState('')
  const [busy, setBusy] = useState(false)
  const [verdict, setVerdict] = useState<LabVerdict | null>(null)
  const [passed, setPassed] = useState(practical.latestPassed)

  async function valider() {
    if (!code.trim()) {
      push('danger', 'Écris ta solution avant de valider.')
      return
    }
    setBusy(true)
    setVerdict(null)
    try {
      const v = await submitPractical(nodeId, [{ path: path.trim() || 'solution.txt', content: code }])
      setVerdict(v)
      setPassed(v.passed)
      push(v.passed ? 'success' : 'danger', v.passed ? 'Épreuve réussie !' : 'Épreuve échouée, réessaie.')
      // rafraîchit l'état persistant (statut de la dernière tentative)
      void getPractical(nodeId).catch(() => undefined)
    } catch (err) {
      push(
        'danger',
        err instanceof ApiError
          ? err.message
          : "Validation impossible : l'environnement de pratique est indisponible.",
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <Card
      title={
        <div className={styles.head}>
          <span>Épreuve pratique{practical.title ? ` : ${practical.title}` : ''}</span>
          {passed ? (
            <Badge tone="success">Réussie</Badge>
          ) : practical.required ? (
            <Badge tone="primary">Requise pour valider</Badge>
          ) : (
            <Badge tone="neutral">Optionnelle</Badge>
          )}
        </div>
      }
    >
      {practical.instructions && (
        <div className={styles.instructions}>
          <RichText text={practical.instructions} />
        </div>
      )}

      <label className={styles.field} htmlFor={fileId}>
        <span className={styles.label}>Fichier</span>
        <input
          id={fileId}
          className={styles.filename}
          value={path}
          onChange={(e) => setPath(e.target.value)}
          spellCheck={false}
          autoCapitalize="off"
          autoCorrect="off"
        />
      </label>

      <label className={styles.field} htmlFor={codeId}>
        <span className={styles.label}>Ta solution</span>
        <textarea
          id={codeId}
          className={styles.editor}
          value={code}
          onChange={(e) => setCode(e.target.value)}
          spellCheck={false}
          rows={14}
          placeholder="Écris ton code ici…"
        />
      </label>

      <div className={styles.actions}>
        <Button onClick={() => void valider()} disabled={busy}>
          {busy ? (
            <span className={styles.busy}>
              <span className={styles.spinner} aria-hidden="true" />
              Validation…
            </span>
          ) : (
            'Valider la pratique'
          )}
        </Button>
        {busy && (
          <span className={styles.hint} role="status">
            Correction en cours, cela peut prendre une quinzaine de secondes.
          </span>
        )}
      </div>

      {verdict && (
        <div
          className={[styles.verdict, verdict.passed ? styles.ok : styles.ko].join(' ')}
          role="status"
          aria-live="polite"
        >
          <strong>{verdict.passed ? 'Réussi' : 'Échoué'}</strong>
          {verdict.output && <pre className={styles.output}>{verdict.output}</pre>}
        </div>
      )}

      {passed && practical.required && (
        <p className={styles.gatingNote}>
          Épreuve réussie. Termine aussi toutes les leçons de ce nœud pour le valider entièrement.
        </p>
      )}
    </Card>
  )
}
