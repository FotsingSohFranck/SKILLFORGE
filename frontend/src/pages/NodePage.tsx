import { useEffect, useState } from 'react'
import { NavLink, useNavigate, useParams } from 'react-router-dom'
import { getNode, listNodeLessons, listRoadmaps } from '../api/catalogue'
import { listNodeChallenges } from '../api/evaluation'
import { getPractical, type LabPractical } from '../api/lab'
import { getNodeLessonProgress, listBookmarks, toggleBookmark } from '../api/learning'
import { ApiError, type ChallengeSummary, type Lesson, type RoadmapNode } from '../api/types'
import { AsyncSection } from '../components/AsyncSection'
import { Badge } from '../components/Badge'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { PracticalPanel } from '../components/PracticalPanel'
import { TrackCover } from '../components/TrackCover'
import { IconBolt, IconCheckCircle, IconCircle, IconStar, IconStarOutline } from '../components/icons'
import { Progress } from '../components/Progress'
import { useToasts } from '../components/toast/toastStore'
import { useAsync } from '../hooks/useAsync'
import styles from './NodePage.module.css'

interface NodeData {
  node: RoadmapNode
  lessons: Lesson[]
  completedIds: string[]
  challenges: ChallengeSummary[]
  practical: LabPractical
  bookmarked: boolean
  roadmapSlug: string | null
}

const PRACTICAL_ABSENT: LabPractical = {
  exists: false,
  required: false,
  title: null,
  instructions: null,
  latestStatus: null,
  latestPassed: false,
}

const IMPORTANCE_LABEL: Record<string, string> = {
  CORE: 'Essentiel',
  RECOMMENDED: 'Recommandé',
  OPTIONAL: 'Optionnel',
}

async function loadNode(id: string): Promise<NodeData> {
  const [node, lessons, progress, challenges, roadmaps, practical] = await Promise.all([
    getNode(id),
    listNodeLessons(id),
    getNodeLessonProgress(id),
    listNodeChallenges(id),
    listRoadmaps().catch(() => []),
    getPractical(id).catch(() => PRACTICAL_ABSENT),
  ])
  let bookmarked = false
  try {
    bookmarked = (await listBookmarks()).some((b) => b.targetType === 'NODE' && b.targetId === id)
  } catch {
    // favoris secondaires
  }
  const roadmapSlug = roadmaps.find((r) => r.id === node.roadmapId)?.slug ?? null
  return { node, lessons, completedIds: progress.completedLessonIds, challenges, practical, bookmarked, roadmapSlug }
}

export function NodePage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const push = useToasts((s) => s.push)
  const state = useAsync(() => loadNode(id), [id])
  const [favorite, setFavorite] = useState(false)

  useEffect(() => {
    if (state.data) setFavorite(state.data.bookmarked)
  }, [state.data])

  async function toggleFavori() {
    const previous = favorite
    setFavorite(!previous)
    try {
      const { bookmarked } = await toggleBookmark('NODE', id)
      setFavorite(bookmarked)
    } catch (err) {
      setFavorite(previous)
      push('danger', err instanceof ApiError ? err.message : 'Action impossible.')
    }
  }

  return (
    <div className={styles.page}>
      <AsyncSection state={state}>
        {({ node, lessons, completedIds, challenges, practical, roadmapSlug }) => {
          const completed = new Set(completedIds)
          const ordered = [...lessons].sort((a, b) => a.position - b.position)
          const percent = ordered.length === 0 ? 0 : (completed.size / ordered.length) * 100
          const next = ordered.find((l) => !completed.has(l.id)) ?? ordered[0]

          return (
            <>
              <Card
                titleAs="h1"
                media={roadmapSlug && <TrackCover slug={roadmapSlug} />}
                title={
                  <div className={styles.head}>
                    <span>{node.title}</span>
                    <button
                      type="button"
                      className={styles.star}
                      aria-pressed={favorite}
                      aria-label={favorite ? 'Retirer des favoris' : 'Ajouter aux favoris'}
                      onClick={() => void toggleFavori()}
                    >
                      {favorite ? <IconStar size={20} /> : <IconStarOutline size={20} />}
                    </button>
                  </div>
                }
              >
                <p className={styles.summary}>{node.summary}</p>
                <div className={styles.meta}>
                  <Badge tone={node.importance === 'CORE' ? 'primary' : 'neutral'}>
                    {IMPORTANCE_LABEL[node.importance] ?? node.importance}
                  </Badge>
                  {node.estimatedMinutes != null && (
                    <span className={styles.minutes}>~{node.estimatedMinutes} min</span>
                  )}
                </div>
                {ordered.length > 0 && (
                  <Progress
                    value={percent}
                    label={`${completed.size} / ${ordered.length} leçons`}
                  />
                )}
                {node.estimatedMinutes != null && ordered.length > 0 && completed.size < ordered.length && (
                  <p className={styles.remaining}>
                    Temps restant estimé : ~
                    {Math.max(
                      1,
                      Math.round((node.estimatedMinutes * (ordered.length - completed.size)) / ordered.length),
                    )}{' '}
                    min
                  </p>
                )}
                {next && (
                  <div className={styles.actions}>
                    <Button onClick={() => navigate(`/lessons/${next.id}`)}>
                      {completed.size === 0 ? 'Commencer' : 'Continuer'}
                    </Button>
                  </div>
                )}
              </Card>

              <ol className={`${styles.lessons} sf-stagger`}>
                {ordered.map((lesson) => {
                  const done = completed.has(lesson.id)
                  return (
                    <li key={lesson.id}>
                      <NavLink
                        to={`/lessons/${lesson.id}`}
                        className={[styles.lesson, 'sf-pressable', done ? styles.done : ''].filter(Boolean).join(' ')}
                      >
                        <span className={styles.check} aria-hidden="true">
                          {done ? <IconCheckCircle size={18} /> : <IconCircle size={18} />}
                        </span>
                        <span className={styles.lessonTitle}>{lesson.title}</span>
                        {done && <Badge tone="success">Terminée</Badge>}
                      </NavLink>
                    </li>
                  )
                })}
              </ol>
              {ordered.length === 0 && (
                <p className={styles.empty}>Ce nœud n'a pas encore de leçon.</p>
              )}

              {challenges.length > 0 && (
                <section className={styles.challenges}>
                  <h2 className={styles.challengesTitle}>Défis de code</h2>
                  <ul className={`${styles.challengeList} sf-stagger`}>
                    {challenges.map((challenge) => (
                      <li key={challenge.id}>
                        <NavLink to={`/challenges/${challenge.id}`} className={`${styles.challenge} sf-pressable`}>
                          <span className={styles.challengeIcon} aria-hidden="true">
                            <IconBolt size={18} />
                          </span>
                          <span className={styles.challengeTitle}>{challenge.title}</span>
                          {challenge.language && <Badge tone="secondary">{challenge.language}</Badge>}
                        </NavLink>
                      </li>
                    ))}
                  </ul>
                </section>
              )}

              {practical.exists && <PracticalPanel nodeId={node.id} practical={practical} />}
            </>
          )
        }}
      </AsyncSection>
    </div>
  )
}
