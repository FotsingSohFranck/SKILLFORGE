import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getCertificationById } from '../api/catalogue'
import {
  getExam,
  getExamHistory,
  getExamRevision,
  startExam,
  submitExam,
} from '../api/evaluation'
import {
  ApiError,
  type DomainResult,
  type Exam,
  type ExamAttempt,
  type ExamResult,
} from '../api/types'
import { AsyncSection } from '../components/AsyncSection'
import { Badge } from '../components/Badge'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { IconClock } from '../components/icons'
import { useToasts } from '../components/toast/toastStore'
import { useAsync } from '../hooks/useAsync'
import styles from './ExamPage.module.css'

type Phase = 'intro' | 'running' | 'result'
type Answer = { selectedOptionId?: string; completed?: boolean }

interface ExamData {
  exam: Exam
  domainNames: Record<string, string>
  history: ExamAttempt[]
  revision: DomainResult[]
}

async function loadExam(id: string): Promise<ExamData> {
  const exam = await getExam(id)
  const [history, revision] = await Promise.all([
    getExamHistory(id).catch(() => [] as ExamAttempt[]),
    getExamRevision(id).catch(() => [] as DomainResult[]),
  ])
  const domainNames: Record<string, string> = {}
  try {
    const cert = await getCertificationById(exam.certificationId)
    cert.domains.forEach((d) => (domainNames[d.id] = d.name))
  } catch {
    // libellés de domaines optionnels
  }
  return { exam, domainNames, history, revision }
}

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function formatDate(iso: string | null): string {
  if (!iso) return '–'
  const date = new Date(iso)
  return Number.isNaN(date.getTime()) ? '–' : date.toLocaleString('fr-FR')
}

export function ExamPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const push = useToasts((s) => s.push)
  const examState = useAsync(() => loadExam(id), [id])

  const [phase, setPhase] = useState<Phase>('intro')
  const [attempt, setAttempt] = useState<ExamAttempt | null>(null)
  const [answers, setAnswers] = useState<Record<string, Answer>>({})
  const [current, setCurrent] = useState(0)
  const [result, setResult] = useState<ExamResult | null>(null)
  const [remaining, setRemaining] = useState<number | null>(null)
  const [busy, setBusy] = useState(false)

  const answersRef = useRef(answers)
  answersRef.current = answers
  const submittedRef = useRef(false)

  const doSubmit = useCallback(
    async (exam: Exam) => {
      if (submittedRef.current || !attempt) return
      submittedRef.current = true
      setBusy(true)
      try {
        const payload = exam.items.map((item) => ({
          itemId: item.id,
          selectedOptionId: answersRef.current[item.id]?.selectedOptionId ?? null,
          completed: answersRef.current[item.id]?.completed ?? null,
        }))
        setResult(await submitExam(attempt.id, payload))
        setPhase('result')
      } catch (err) {
        submittedRef.current = false
        push('danger', err instanceof ApiError ? err.message : 'Soumission impossible.')
      } finally {
        setBusy(false)
      }
    },
    [attempt, push],
  )

  // Minuteur : décompte jusqu'à la deadline, auto-soumission à l'expiration.
  useEffect(() => {
    if (phase !== 'running' || !attempt?.deadlineAt) return
    const deadline = new Date(attempt.deadlineAt).getTime()
    const exam = examState.data?.exam
    const tick = () => {
      const secs = Math.max(0, Math.round((deadline - Date.now()) / 1000))
      setRemaining(secs)
      if (secs <= 0 && exam) void doSubmit(exam)
    }
    tick()
    const iv = setInterval(tick, 1000)
    return () => clearInterval(iv)
  }, [phase, attempt, examState.data, doSubmit])

  async function start() {
    setBusy(true)
    try {
      const a = await startExam(id)
      submittedRef.current = false
      setAttempt(a)
      setAnswers({})
      setCurrent(0)
      setResult(null)
      setPhase('running')
    } catch (err) {
      push('danger', err instanceof ApiError ? err.message : "Impossible de démarrer l'examen.")
    } finally {
      setBusy(false)
    }
  }

  function restart() {
    examState.reload() // rafraîchit l'historique et la révision avec la tentative qui vient d'être soumise
    setPhase('intro')
  }

  return (
    <div className={styles.page}>
      <AsyncSection state={examState}>
        {({ exam, domainNames, history, revision }) => {
          if (phase === 'intro') {
            return (
              <Intro
                exam={exam}
                history={history}
                revision={revision}
                domainNames={domainNames}
                busy={busy}
                onStart={() => void start()}
              />
            )
          }
          if (phase === 'result' && result) {
            return (
              <ResultView
                exam={exam}
                result={result}
                domainNames={domainNames}
                onRestart={restart}
                onBack={() => navigate('/catalogue')}
              />
            )
          }
          return (
            <Running
              exam={exam}
              answers={answers}
              current={current}
              remaining={remaining}
              busy={busy}
              onSelectOption={(itemId, optionId) =>
                setAnswers((prev) => ({ ...prev, [itemId]: { selectedOptionId: optionId } }))
              }
              onToggleTask={(itemId, completed) =>
                setAnswers((prev) => ({ ...prev, [itemId]: { completed } }))
              }
              onGoto={setCurrent}
              onSubmit={() => void doSubmit(exam)}
            />
          )
        }}
      </AsyncSection>
    </div>
  )
}

interface IntroProps {
  exam: Exam
  history: ExamAttempt[]
  revision: DomainResult[]
  domainNames: Record<string, string>
  busy: boolean
  onStart: () => void
}

function Intro({ exam, history, revision, domainNames, busy, onStart }: IntroProps) {
  const submitted = history.filter((a) => a.status !== 'IN_PROGRESS')
  return (
    <>
      <Card title={exam.title}>
        <p className={styles.introMeta}>
          Format {exam.format} · {exam.durationMinutes} minutes · {exam.items.length} questions
        </p>
        <p className={styles.introHint}>
          Le minuteur démarre dès le lancement. À l'expiration, l'examen est soumis automatiquement.
        </p>
        <Button onClick={onStart} disabled={busy}>
          {busy ? 'Démarrage…' : submitted.length > 0 ? 'Repasser l’examen' : "Démarrer l'examen"}
        </Button>
      </Card>

      {revision.length > 0 && (
        <Card title="Révision ciblée">
          <p className={styles.introHint}>
            Concentrez-vous sur vos points faibles avant de repasser l'examen :
          </p>
          <div className={styles.weakList}>
            {revision.map((domain) => (
              <div key={domain.domainId} className={styles.weakRow}>
                <span className={styles.weakName}>{domainNames[domain.domainId] ?? 'Domaine'}</span>
                <Badge tone="warning">Point faible</Badge>
              </div>
            ))}
          </div>
        </Card>
      )}

      {submitted.length > 0 && (
        <section className={styles.history}>
          <h2 className={styles.historyTitle}>Vos tentatives ({submitted.length})</h2>
          {submitted.map((a) => {
            const percent = a.maxScore > 0 ? Math.round((a.totalScore / a.maxScore) * 100) : 0
            return (
              <div key={a.id} className={styles.historyRow}>
                <span className={styles.historyDate}>{formatDate(a.submittedAt ?? a.startedAt)}</span>
                <span className={styles.historyScore}>
                  {a.totalScore} / {a.maxScore} ({percent}%)
                </span>
                <Badge tone={a.passed ? 'success' : 'danger'}>{a.passed ? 'Réussi' : 'Échoué'}</Badge>
                {a.status === 'EXPIRED' && <Badge tone="warning">Temps écoulé</Badge>}
              </div>
            )
          })}
        </section>
      )}
    </>
  )
}

interface RunningProps {
  exam: Exam
  answers: Record<string, Answer>
  current: number
  remaining: number | null
  busy: boolean
  onSelectOption: (itemId: string, optionId: string) => void
  onToggleTask: (itemId: string, completed: boolean) => void
  onGoto: (index: number) => void
  onSubmit: () => void
}

function isAnswered(answer: Answer | undefined): boolean {
  return answer != null && (answer.selectedOptionId != null || answer.completed === true)
}

function Running({
  exam,
  answers,
  current,
  remaining,
  busy,
  onSelectOption,
  onToggleTask,
  onGoto,
  onSubmit,
}: RunningProps) {
  const items = [...exam.items].sort((a, b) => a.position - b.position)
  const item = items[current]
  const answer = answers[item.id]
  const low = remaining != null && remaining <= 60

  return (
    <>
      <div className={styles.bar}>
        <span className={[styles.timer, low ? styles.timerLow : ''].filter(Boolean).join(' ')}>
          <IconClock size={16} /> {remaining != null ? formatTime(remaining) : '--:--'}
        </span>
        <span className={styles.counter}>
          Question {current + 1} / {items.length}
        </span>
        <Button size="sm" onClick={onSubmit} disabled={busy}>
          Terminer
        </Button>
      </div>

      <div className={styles.palette}>
        {items.map((it, index) => (
          <button
            key={it.id}
            type="button"
            className={[
              styles.dot,
              index === current ? styles.dotCurrent : '',
              isAnswered(answers[it.id]) ? styles.dotDone : '',
            ]
              .filter(Boolean)
              .join(' ')}
            onClick={() => onGoto(index)}
            aria-label={`Question ${index + 1}`}
          >
            {index + 1}
          </button>
        ))}
      </div>

      <Card>
        <p className={styles.prompt}>{item.prompt}</p>
        {item.kind === 'TASK' ? (
          <label className={styles.task}>
            <input
              type="checkbox"
              checked={answer?.completed === true}
              onChange={(e) => onToggleTask(item.id, e.target.checked)}
            />
            <span>J'ai réalisé cette tâche</span>
          </label>
        ) : (
          <div className={styles.options}>
            {[...item.options]
              .sort((a, b) => a.position - b.position)
              .map((option) => (
                <label
                  key={option.id}
                  className={[
                    styles.option,
                    answer?.selectedOptionId === option.id ? styles.optionSelected : '',
                  ]
                    .filter(Boolean)
                    .join(' ')}
                >
                  <input
                    type="radio"
                    name={item.id}
                    checked={answer?.selectedOptionId === option.id}
                    onChange={() => onSelectOption(item.id, option.id)}
                  />
                  <span>{option.label}</span>
                </label>
              ))}
          </div>
        )}
      </Card>

      <div className={styles.pager}>
        <Button variant="ghost" disabled={current === 0} onClick={() => onGoto(current - 1)}>
          ← Précédent
        </Button>
        <Button
          variant="ghost"
          disabled={current === items.length - 1}
          onClick={() => onGoto(current + 1)}
        >
          Suivant →
        </Button>
      </div>
    </>
  )
}

interface ResultProps {
  exam: Exam
  result: ExamResult
  domainNames: Record<string, string>
  onRestart: () => void
  onBack: () => void
}

function ResultView({ exam, result, domainNames, onRestart, onBack }: ResultProps) {
  const percent = result.maxScore > 0 ? Math.round((result.totalScore / result.maxScore) * 100) : 0
  return (
    <>
      <Card title={`Résultat : ${exam.title}`}>
        <div className={styles.scoreRow}>
          <span className={styles.score}>
            {result.totalScore} / {result.maxScore} ({percent}%)
          </span>
          <Badge tone={result.passed ? 'success' : 'danger'}>
            {result.passed ? 'Réussi' : 'Échoué'}
          </Badge>
          {result.expired && <Badge tone="warning">Temps écoulé</Badge>}
        </div>
      </Card>

      <section className={styles.domains}>
        <h2 className={styles.domainsTitle}>Score par domaine</h2>
        {result.domains.map((domain) => {
          const dPercent = domain.maxScore > 0 ? Math.round((domain.score / domain.maxScore) * 100) : 0
          return (
            <div key={domain.domainId} className={styles.domainRow}>
              <span className={styles.domainName}>
                {domainNames[domain.domainId] ?? 'Domaine'}
              </span>
              <span className={styles.domainScore}>
                {domain.score} / {domain.maxScore} ({dPercent}%)
              </span>
              {domain.weak && <Badge tone="warning">Point faible</Badge>}
            </div>
          )
        })}
      </section>

      <div className={styles.pager}>
        <Button variant="secondary" onClick={onRestart}>
          Recommencer
        </Button>
        <Button variant="ghost" onClick={onBack}>
          Retour au catalogue
        </Button>
      </div>
    </>
  )
}
