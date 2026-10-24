import { useState } from 'react'
import { getPractical, submitPractical, type LabPractical, type LabVerdict } from '../api/lab'
import { ApiError } from '../api/types'
import { useToasts } from './toast/toastStore'
import { Badge } from './Badge'
import { Button } from './Button'
import { Card } from './Card'
import styles from './PracticalPanel.module.css'

interface Props {
  nodeId: string
  practical: LabPractical
}

/** Épreuve pratique d'un nœud : consigne, éditeur de code, validation et verdict. */
export function PracticalPanel({ nodeId, practical }: Props) {
  const push = useToasts((s) => s.push)
  const [path, setPath] = useState('solution.txt')
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
      {practical.instructions && <div className={styles.instructions}>{practical.instructions}</div>}

      <label className={styles.field}>
        <span className={styles.label}>Fichier</span>
        <input
          className={styles.filename}
          value={path}
          onChange={(e) => setPath(e.target.value)}
          spellCheck={false}
          autoCapitalize="off"
          autoCorrect="off"
        />
      </label>

      <label className={styles.field}>
        <span className={styles.label}>Ta solution</span>
        <textarea
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
          {busy ? 'Validation…' : 'Valider la pratique'}
        </Button>
      </div>

      {verdict && (
        <div className={[styles.verdict, verdict.passed ? styles.ok : styles.ko].join(' ')}>
          <strong>{verdict.passed ? 'Réussi' : 'Échoué'}</strong>
          {verdict.output && <pre className={styles.output}>{verdict.output}</pre>}
        </div>
      )}
    </Card>
  )
}
