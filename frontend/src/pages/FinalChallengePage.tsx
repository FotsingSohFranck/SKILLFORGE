import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  type Attempt,
  type GuidanceLevel,
  startChallenge,
  submitDecision,
} from '../api/challenge'
import { ApiError } from '../api/types'
import { Button } from '../components/Button'
import styles from './FinalChallengePage.module.css'

type Phase = 'loading' | 'error' | 'briefing' | 'playing' | 'result'

interface LogEntry {
  node: string
  choice: string
}

const GUIDANCE: Record<GuidanceLevel, { label: string; tone: string }> = {
  FAVORABLE: { label: 'Trajectoire favorable', tone: 'favorable' },
  A_SURVEILLER: { label: 'À surveiller', tone: 'surveiller' },
  DEFAVORABLE: { label: 'Trajectoire défavorable', tone: 'defavorable' },
}

export function FinalChallengePage() {
  const { nodeId = '' } = useParams()
  const navigate = useNavigate()
  const [phase, setPhase] = useState<Phase>('loading')
  const [attempt, setAttempt] = useState<Attempt | null>(null)
  const [selected, setSelected] = useState<string | null>(null)
  const [log, setLog] = useState<LogEntry[]>([])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const started = useRef(false)

  function begin() {
    setPhase('loading')
    setLog([])
    setSelected(null)
    setAttempt(null)
    setError(null)
    startChallenge(nodeId)
      .then((a) => {
        setAttempt(a)
        setPhase('briefing')
      })
      .catch((e) => {
        setError(e instanceof ApiError ? e.message : 'Impossible de démarrer le Final Challenge.')
        setPhase('error')
      })
  }

  useEffect(() => {
    if (started.current) return
    started.current = true
    begin()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [nodeId])

  async function choose() {
    if (!attempt?.node || !selected || submitting) return
    const chosen = attempt.node.choices.find((c) => c.id === selected)
    const nodeTitle = attempt.node.title
    setSubmitting(true)
    setError(null)
    try {
      const next = await submitDecision(attempt.attemptId, selected)
      setLog((l) => [...l, { node: nodeTitle, choice: chosen?.label ?? selected }])
      setAttempt(next)
      setSelected(null)
      setPhase(next.status === 'COMPLETED' ? 'result' : 'playing')
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Décision refusée.')
    } finally {
      setSubmitting(false)
    }
  }

  const tone =
    phase === 'result'
      ? attempt?.result?.passed
        ? 'favorable'
        : 'defavorable'
      : attempt?.guidance
        ? GUIDANCE[attempt.guidance].tone
        : 'neutral'

  return (
    <div className={styles.stage} data-tone={tone}>
      <div className={styles.ambient} aria-hidden="true" />
      <div className={styles.frame}>
        {phase === 'loading' && <p className={styles.loading}>Préparation de la mission…</p>}

        {phase === 'error' && (
          <div className={styles.panel}>
            <p className={styles.errorText}>{error}</p>
            <Button variant="secondary" onClick={() => navigate(`/nodes/${nodeId}`)}>
              Revenir au nœud
            </Button>
          </div>
        )}

        {phase === 'briefing' && attempt && (
          <div className={styles.panel}>
            <p className={styles.kicker}>Final Challenge</p>
            <h1 className={styles.missionTitle}>{attempt.objective ?? 'Mission'}</h1>
            {attempt.briefing && <p className={styles.briefing}>{attempt.briefing}</p>}
            <div className={styles.actions}>
              <Button onClick={() => setPhase('playing')}>Commencer la mission</Button>
              <Button variant="ghost" onClick={() => navigate(`/nodes/${nodeId}`)}>
                Plus tard
              </Button>
            </div>
          </div>
        )}

        {phase === 'playing' && attempt?.node && (
          <div className={styles.play}>
            <header className={styles.topbar}>
              <button
                type="button"
                className={styles.quit}
                onClick={() => navigate(`/nodes/${nodeId}`)}
              >
                ← Quitter
              </button>
              <GuidancePill guidance={attempt.guidance} />
              <span className={styles.counter}>Décision {log.length + 1}</span>
            </header>

            <section className={styles.scene} aria-live="polite">
              <h2 className={styles.sceneTitle}>{attempt.node.title}</h2>
              {attempt.node.context && <p className={styles.context}>{attempt.node.context}</p>}
              {attempt.node.event && <p className={styles.event}>{attempt.node.event}</p>}

              <ul className={styles.choices}>
                {attempt.node.choices.map((c) => {
                  const active = selected === c.id
                  return (
                    <li key={c.id}>
                      <button
                        type="button"
                        className={[styles.choice, active ? styles.choiceActive : '']
                          .filter(Boolean)
                          .join(' ')}
                        aria-pressed={active}
                        onClick={() => setSelected(c.id)}
                      >
                        <span className={styles.choiceDot} aria-hidden="true" />
                        <span>{c.label}</span>
                      </button>
                    </li>
                  )
                })}
              </ul>

              {error && <p className={styles.errorText}>{error}</p>}

              <div className={styles.actions}>
                <Button onClick={() => void choose()} disabled={!selected || submitting}>
                  {submitting ? 'Validation…' : 'Valider ma décision'}
                </Button>
              </div>
            </section>

            {log.length > 0 && (
              <aside className={styles.journal} aria-label="Journal de mission">
                <h3 className={styles.journalTitle}>Journal de mission</h3>
                <ol className={styles.journalList}>
                  {log.map((e, i) => (
                    <li key={i} className={styles.journalItem}>
                      <span className={styles.journalNode}>{e.node}</span>
                      <span className={styles.journalChoice}>{e.choice}</span>
                    </li>
                  ))}
                </ol>
              </aside>
            )}
          </div>
        )}

        {phase === 'result' && attempt?.result && (
          <Report
            result={attempt.result}
            onReplay={begin}
            onBack={() => navigate(`/nodes/${nodeId}`)}
          />
        )}
      </div>
    </div>
  )
}

function GuidancePill({ guidance }: { guidance: GuidanceLevel | null }) {
  const meta = guidance ? GUIDANCE[guidance] : { label: 'Trajectoire en cours', tone: 'neutral' }
  return (
    <span className={styles.pill} data-tone={meta.tone}>
      <span className={styles.pillDot} aria-hidden="true" />
      {meta.label}
    </span>
  )
}

function Report({
  result,
  onReplay,
  onBack,
}: {
  result: NonNullable<Attempt['result']>
  onReplay: () => void
  onBack: () => void
}) {
  const verdict = result.eliminated
    ? { label: 'Mission compromise', tone: 'defavorable', note: 'Une condition éliminatoire a été déclenchée.' }
    : result.passed
      ? { label: 'Mission réussie', tone: 'favorable', note: 'Objectif atteint.' }
      : result.reinforce
        ? { label: 'À renforcer', tone: 'surveiller', note: 'Presque : quelques compétences à consolider.' }
        : { label: 'Mission échouée', tone: 'defavorable', note: 'Score insuffisant.' }

  return (
    <div className={styles.report}>
      <div className={styles.verdict} data-tone={verdict.tone}>
        <p className={styles.verdictLabel}>{verdict.label}</p>
        <p className={styles.score}>{result.globalScore}%</p>
        <p className={styles.verdictNote}>{verdict.note}</p>
      </div>

      <section className={styles.competencies}>
        <h3 className={styles.competenciesTitle}>Compétences évaluées</h3>
        {result.competencies.map((c) => (
          <div key={c.competency} className={styles.comp}>
            <div className={styles.compHead}>
              <span>{c.competency}</span>
              <span className={styles.compPct}>{c.percent}%</span>
            </div>
            <div className={styles.bar}>
              <span className={styles.barFill} style={{ width: `${c.percent}%` }} />
            </div>
          </div>
        ))}
      </section>

      {result.triggeredCritical.length > 0 && (
        <p className={styles.critical}>
          Conditions éliminatoires : {result.triggeredCritical.join(', ')}
        </p>
      )}

      <div className={styles.actions}>
        <Button onClick={onBack}>Revenir au nœud</Button>
        <Button variant="secondary" onClick={onReplay}>
          Rejouer
        </Button>
      </div>
    </div>
  )
}
