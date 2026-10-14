import { useMemo, useState } from 'react'
import { getQuiz, submitQuizAttempt } from '../api/evaluation'
import { ApiError, type AnswerResult, type AttemptResult } from '../api/types'
import { useAsync } from '../hooks/useAsync'
import { AsyncSection } from './AsyncSection'
import { Button } from './Button'
import { IconCheck, IconCheckCircle, IconCross, IconWarning } from './icons'
import { deEmDash } from '../format'
import { useToasts } from './toast/toastStore'
import styles from './QuizRunner.module.css'

interface QuizRunnerProps {
  quizId: string
}

/** Quiz interactif inline : sélection d'une option par question, correction immédiate. */
export function QuizRunner({ quizId }: QuizRunnerProps) {
  const push = useToasts((s) => s.push)
  const state = useAsync(() => getQuiz(quizId), [quizId])
  const [selected, setSelected] = useState<Record<string, string>>({})
  const [result, setResult] = useState<AttemptResult | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const resultByQuestion = useMemo(() => {
    const map = new Map<string, AnswerResult>()
    result?.answers.forEach((a) => map.set(a.questionId, a))
    return map
  }, [result])

  async function submit() {
    if (!state.data) return
    const answers = Object.entries(selected).map(([questionId, optionId]) => ({ questionId, optionId }))
    setSubmitting(true)
    try {
      setResult(await submitQuizAttempt(quizId, answers))
    } catch (err) {
      push('danger', err instanceof ApiError ? err.message : 'Envoi impossible.')
    } finally {
      setSubmitting(false)
    }
  }

  function reset() {
    setSelected({})
    setResult(null)
  }

  return (
    <div className={styles.quiz}>
      <AsyncSection state={state}>
        {(quiz) => {
          const questions = [...quiz.questions].sort((a, b) => a.position - b.position)
          const allAnswered = questions.every((q) => selected[q.id])

          return (
            <>
              <div className={styles.header}>
                <span className={styles.badge}>Quiz</span>
                <span className={styles.title}>{deEmDash(quiz.title)}</span>
                {result && (
                  <span className={styles.score}>
                    Score : {result.score} / {result.maxScore}
                  </span>
                )}
              </div>

              <ol className={styles.questions}>
                {questions.map((question) => {
                  const answer = resultByQuestion.get(question.id)
                  return (
                    <li key={question.id} className={styles.question}>
                      <p className={styles.prompt}>{deEmDash(question.prompt)}</p>
                      <div className={styles.options}>
                        {[...question.options]
                          .sort((a, b) => a.position - b.position)
                          .map((option) => {
                            const isSelected = selected[question.id] === option.id
                            const isCorrect = answer?.correctOptionId === option.id
                            const isWrongPick = answer && answer.selectedOptionId === option.id && !answer.correct
                            const cls = [
                              styles.option,
                              result == null ? 'sf-pressable' : '',
                              isSelected ? styles.selected : '',
                              answer && isCorrect ? styles.correct : '',
                              isWrongPick ? styles.wrong : '',
                            ]
                              .filter(Boolean)
                              .join(' ')
                            return (
                              <label key={option.id} className={cls}>
                                <input
                                  type="radio"
                                  name={question.id}
                                  value={option.id}
                                  checked={isSelected}
                                  disabled={result != null}
                                  onChange={() =>
                                    setSelected((prev) => ({ ...prev, [question.id]: option.id }))
                                  }
                                />
                                <span className={styles.optionLabel}>{deEmDash(option.label)}</span>
                                {answer && isCorrect && (
                                  <span className={`${styles.marker} ${styles.markerCorrect}`}>
                                    <IconCheck size={14} /> Bonne réponse
                                  </span>
                                )}
                                {isWrongPick && (
                                  <span className={`${styles.marker} ${styles.markerWrong}`}>
                                    <IconCross size={14} /> Votre réponse
                                  </span>
                                )}
                              </label>
                            )
                          })}
                      </div>
                      {answer?.justification && (
                        <p className={styles.justification}>{deEmDash(answer.justification)}</p>
                      )}
                    </li>
                  )
                })}
              </ol>

              {result &&
                (() => {
                  const ratio = result.maxScore > 0 ? result.score / result.maxScore : 0
                  const tone = ratio === 1 ? 'success' : ratio >= 0.5 ? 'warning' : 'danger'
                  const message =
                    ratio === 1
                      ? 'Sans faute, bravo. Tu peux passer à la suite.'
                      : ratio >= 0.5
                        ? 'Bien joué. Revois les points manqués ci-dessus, puis continue.'
                        : 'Reprends la leçon, puis retente le quiz pour consolider.'
                  return (
                    <div className={styles.resultBanner} data-tone={tone}>
                      <span className={styles.resultIcon} aria-hidden="true">
                        {tone === 'success' ? <IconCheckCircle size={22} /> : <IconWarning size={22} />}
                      </span>
                      <div>
                        <div className={styles.resultScore}>
                          Score : {result.score} / {result.maxScore}
                        </div>
                        <div className={styles.resultMsg}>{message}</div>
                      </div>
                    </div>
                  )
                })()}

              <div className={styles.actions}>
                {result ? (
                  <Button variant="secondary" onClick={reset}>
                    Recommencer
                  </Button>
                ) : (
                  <Button onClick={() => void submit()} disabled={submitting || !allAnswered}>
                    {submitting ? 'Correction…' : 'Valider mes réponses'}
                  </Button>
                )}
              </div>
            </>
          )
        }}
      </AsyncSection>
    </div>
  )
}
