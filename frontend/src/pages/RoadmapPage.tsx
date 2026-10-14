import { useEffect, useMemo, useState } from 'react'
import { NavLink, useParams } from 'react-router-dom'
import { getCertification, getRoadmap } from '../api/catalogue'
import { listCertificationExams } from '../api/evaluation'
import { enroll, getRoadmapProgress, listBookmarks, toggleBookmark } from '../api/learning'
import {
  ApiError,
  type ExamSummary,
  type NodeProgress,
  type RoadmapGraph,
  type RoadmapNode,
  type RoadmapProgress,
} from '../api/types'
import { AsyncSection } from '../components/AsyncSection'
import { Badge, type BadgeTone } from '../components/Badge'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { IconClock, IconStar, IconStarOutline } from '../components/icons'
import { Progress } from '../components/Progress'
import { TrackBadge } from '../components/TrackBadge'
import { TrackCover } from '../components/TrackCover'
import { useToasts } from '../components/toast/toastStore'
import { useAsync } from '../hooks/useAsync'
import styles from './RoadmapPage.module.css'

interface RoadmapData {
  graph: RoadmapGraph
  progress: RoadmapProgress | null
  bookmarkedNodeIds: string[]
  exams: ExamSummary[]
}

const IMPORTANCE_LABEL: Record<string, string> = {
  CORE: 'Essentiel',
  RECOMMENDED: 'Recommandé',
  OPTIONAL: 'Optionnel',
}

const STATUS_META: Record<string, { label: string; tone: BadgeTone }> = {
  NOT_STARTED: { label: 'Non commencé', tone: 'neutral' },
  IN_PROGRESS: { label: 'En cours', tone: 'warning' },
  COMPLETED: { label: 'Terminé', tone: 'success' },
}

/** Formate une durée en minutes : « 2 h 05 » ou « 45 min ». */
function formatDuration(minutes: number): string {
  const total = Math.max(1, Math.round(minutes))
  if (total < 60) return `${total} min`
  const h = Math.floor(total / 60)
  const m = total % 60
  return m === 0 ? `${h} h` : `${h} h ${String(m).padStart(2, '0')}`
}

async function loadRoadmap(slug: string): Promise<RoadmapData> {
  const graph = await getRoadmap(slug)
  let progress: RoadmapProgress | null = null
  try {
    progress = await getRoadmapProgress(graph.roadmap.id)
  } catch (err) {
    if (!(err instanceof ApiError && err.code === 'NOT_ENROLLED')) throw err
  }
  let bookmarkedNodeIds: string[] = []
  try {
    const bookmarks = await listBookmarks()
    bookmarkedNodeIds = bookmarks.filter((b) => b.targetType === 'NODE').map((b) => b.targetId)
  } catch {
    // les favoris sont secondaires : on ignore une éventuelle erreur
  }
  // Parcours de certification : découvrir les examens blancs (best-effort).
  let exams: ExamSummary[] = []
  if (graph.roadmap.kind === 'CERTIFICATION') {
    try {
      const certification = await getCertification(graph.roadmap.slug)
      exams = await listCertificationExams(certification.id)
    } catch {
      // pas de certification/examens : section masquée
    }
  }
  return { graph, progress, bookmarkedNodeIds, exams }
}

export function RoadmapPage() {
  const { slug = '' } = useParams()
  const push = useToasts((s) => s.push)
  const state = useAsync(() => loadRoadmap(slug), [slug])
  const [enrolling, setEnrolling] = useState(false)
  const [bookmarked, setBookmarked] = useState<Set<string>>(new Set())

  useEffect(() => {
    if (state.data) setBookmarked(new Set(state.data.bookmarkedNodeIds))
  }, [state.data])

  async function start(roadmapId: string) {
    setEnrolling(true)
    try {
      await enroll(roadmapId)
      state.reload()
    } catch (err) {
      push('danger', err instanceof ApiError ? err.message : "L'inscription a échoué.")
    } finally {
      setEnrolling(false)
    }
  }

  async function toggleFavori(nodeId: string) {
    // bascule optimiste, réalignée sur la réponse serveur
    const optimistic = new Set(bookmarked)
    optimistic.has(nodeId) ? optimistic.delete(nodeId) : optimistic.add(nodeId)
    setBookmarked(optimistic)
    try {
      const { bookmarked: isOn } = await toggleBookmark('NODE', nodeId)
      setBookmarked((prev) => {
        const next = new Set(prev)
        isOn ? next.add(nodeId) : next.delete(nodeId)
        return next
      })
    } catch (err) {
      setBookmarked(new Set(bookmarked))
      push('danger', err instanceof ApiError ? err.message : 'Action impossible.')
    }
  }

  return (
    <div className={styles.page}>
      <AsyncSection state={state}>
        {({ graph, progress, exams }) => (
          <RoadmapView
            graph={graph}
            progress={progress}
            exams={exams}
            enrolling={enrolling}
            bookmarked={bookmarked}
            onEnroll={() => void start(graph.roadmap.id)}
            onToggleFavori={(id) => void toggleFavori(id)}
          />
        )}
      </AsyncSection>
    </div>
  )
}

interface RoadmapViewProps {
  graph: RoadmapGraph
  progress: RoadmapProgress | null
  exams: ExamSummary[]
  enrolling: boolean
  bookmarked: Set<string>
  onEnroll: () => void
  onToggleFavori: (nodeId: string) => void
}

function RoadmapView({
  graph,
  progress,
  exams,
  enrolling,
  bookmarked,
  onEnroll,
  onToggleFavori,
}: RoadmapViewProps) {
  const progressByNode = useMemo(() => {
    const map = new Map<string, NodeProgress>()
    progress?.nodes.forEach((n) => map.set(n.nodeId, n))
    return map
  }, [progress])

  // Prérequis : titres des nœuds sources des arêtes entrantes.
  const nodeTitleById = useMemo(() => {
    const map = new Map<string, string>()
    graph.nodes.forEach((n) => map.set(n.id, n.title))
    return map
  }, [graph.nodes])

  const prereqTitlesByNode = useMemo(() => {
    const map = new Map<string, string[]>()
    graph.edges.forEach((edge) => {
      const source = nodeTitleById.get(edge.sourceNodeId)
      if (!source) return
      const list = map.get(edge.targetNodeId) ?? []
      list.push(source)
      map.set(edge.targetNodeId, list)
    })
    return map
  }, [graph.edges, nodeTitleById])

  const prereqIdsByNode = useMemo(() => {
    const map = new Map<string, string[]>()
    graph.edges.forEach((edge) => {
      const list = map.get(edge.targetNodeId) ?? []
      list.push(edge.sourceNodeId)
      map.set(edge.targetNodeId, list)
    })
    return map
  }, [graph.edges])

  // Temps restant estimé du parcours = somme des minutes des nœuds pondérée par ce qui reste.
  const trackRemaining = useMemo(() => {
    let remaining = 0
    graph.nodes.forEach((n) => {
      if (n.estimatedMinutes == null) return
      const pct = progressByNode.get(n.id)?.percent ?? 0
      remaining += n.estimatedMinutes * (1 - Math.min(100, Math.max(0, pct)) / 100)
    })
    return remaining
  }, [graph.nodes, progressByNode])

  const enrolled = progress != null
  // Un nœud est "à débloquer" (advisory, non bloquant) quand on est inscrit, qu'il
  // n'est pas commencé, et qu'au moins un prérequis n'est pas terminé.
  const isLocked = (nodeId: string): boolean => {
    if (!enrolled) return false
    if ((progressByNode.get(nodeId)?.status ?? 'NOT_STARTED') !== 'NOT_STARTED') return false
    const prereqs = prereqIdsByNode.get(nodeId) ?? []
    if (prereqs.length === 0) return false
    return prereqs.some((pid) => (progressByNode.get(pid)?.status ?? 'NOT_STARTED') !== 'COMPLETED')
  }

  const phases = useMemo(() => [...graph.phases].sort((a, b) => a.position - b.position), [graph.phases])
  const nodesByPhase = useMemo(() => {
    const map = new Map<string, RoadmapNode[]>()
    graph.nodes.forEach((n) => {
      const list = map.get(n.phaseId) ?? []
      list.push(n)
      map.set(n.phaseId, list)
    })
    map.forEach((list) => list.sort((a, b) => a.position - b.position))
    return map
  }, [graph.nodes])

  return (
    <>
      <Card
        titleAs="h1"
        media={<TrackCover slug={graph.roadmap.slug} tall />}
        title={
          <div className={styles.head}>
            <span className={styles.headMain}>
              <TrackBadge slug={graph.roadmap.slug} title={graph.roadmap.title} size={36} />
              <span>{graph.roadmap.title}</span>
            </span>
            <Badge tone={graph.roadmap.kind === 'CERTIFICATION' ? 'secondary' : 'primary'}>
              {graph.roadmap.kind === 'CERTIFICATION' ? 'Certification' : 'Parcours'}
            </Badge>
          </div>
        }
      >
        <p className={styles.summary}>{graph.roadmap.summary}</p>
        {enrolled ? (
          <>
            <Progress
              value={progress!.enrollment.progressPercent}
              label="Progression du parcours"
            />
            {progress!.enrollment.progressPercent >= 100 ? (
              <div className={styles.certRow}>
                <Badge tone="success">Parcours terminé</Badge>
                <NavLink to={`/certificate/${graph.roadmap.slug}`}>
                  <Button size="sm">Voir mon certificat</Button>
                </NavLink>
              </div>
            ) : (
              trackRemaining > 0 && (
                <p className={styles.remaining}>
                  Temps restant estimé : ~{formatDuration(trackRemaining)}
                </p>
              )
            )}
          </>
        ) : (
          <Button onClick={onEnroll} disabled={enrolling}>
            {enrolling ? 'Inscription…' : 'Commencer le parcours'}
          </Button>
        )}
      </Card>

      {exams.length > 0 && (
        <section className={styles.exams}>
          <h2 className={styles.phaseTitle}>Examens blancs</h2>
          <div className={styles.examList}>
            {exams.map((exam) => (
              <NavLink key={exam.id} to={`/exams/${exam.id}`} className={styles.exam}>
                <span className={styles.examIcon} aria-hidden="true">
                  <IconClock size={18} />
                </span>
                <span className={styles.examTitle}>{exam.title}</span>
                <span className={styles.examMeta}>
                  {exam.format} · {exam.durationMinutes} min
                </span>
              </NavLink>
            ))}
          </div>
        </section>
      )}

      <div className={styles.phases}>
        {phases.map((phase) => {
          const phaseNodes = nodesByPhase.get(phase.id) ?? []
          const doneCount = phaseNodes.filter(
            (n) => (progressByNode.get(n.id)?.status ?? 'NOT_STARTED') === 'COMPLETED',
          ).length
          return (
            <section key={phase.id} className={styles.phase}>
              <div className={styles.phaseHead}>
                <h2 className={styles.phaseTitle}>{phase.title}</h2>
                {enrolled && phaseNodes.length > 0 && (
                  <span className={styles.phaseCount}>
                    {doneCount} / {phaseNodes.length} terminés
                  </span>
                )}
              </div>
              <div className={`${styles.nodes} sf-stagger`}>
                {phaseNodes.map((node) => (
                  <NodeItem
                    key={node.id}
                    node={node}
                    progress={progressByNode.get(node.id) ?? null}
                    prerequisites={prereqTitlesByNode.get(node.id) ?? []}
                    locked={isLocked(node.id)}
                    favorite={bookmarked.has(node.id)}
                    onToggleFavori={() => onToggleFavori(node.id)}
                  />
                ))}
              </div>
            </section>
          )
        })}
      </div>
    </>
  )
}

interface NodeItemProps {
  node: RoadmapNode
  progress: NodeProgress | null
  prerequisites: string[]
  locked: boolean
  favorite: boolean
  onToggleFavori: () => void
}

function NodeItem({ node, progress, prerequisites, locked, favorite, onToggleFavori }: NodeItemProps) {
  const status = progress?.status ?? 'NOT_STARTED'
  const meta = STATUS_META[status] ?? STATUS_META.NOT_STARTED

  return (
    <div
      className={[styles.node, styles[status.toLowerCase()], locked ? styles.locked : 'sf-lift']
        .filter(Boolean)
        .join(' ')}
    >
      <div className={styles.nodeTop}>
        <NavLink to={`/nodes/${node.id}`} className={styles.nodeTitle}>
          {node.title}
        </NavLink>
        <button
          type="button"
          className={styles.star}
          aria-pressed={favorite}
          aria-label={favorite ? 'Retirer des favoris' : 'Ajouter aux favoris'}
          onClick={onToggleFavori}
        >
          {favorite ? <IconStar size={20} /> : <IconStarOutline size={20} />}
        </button>
      </div>

      <div className={styles.nodeMeta}>
        <Badge tone={node.importance === 'CORE' ? 'primary' : 'neutral'}>
          {IMPORTANCE_LABEL[node.importance] ?? node.importance}
        </Badge>
        {node.estimatedMinutes != null && (
          <span className={styles.minutes}>~{node.estimatedMinutes} min</span>
        )}
        <Badge tone={meta.tone}>{meta.label}</Badge>
        {locked && <Badge tone="neutral">Prérequis à terminer</Badge>}
      </div>

      {status !== 'NOT_STARTED' && progress && (
        <Progress value={progress.percent} showPercent />
      )}

      {prerequisites.length > 0 && (
        <div className={styles.prereq}>Prérequis : {prerequisites.join(', ')}</div>
      )}
    </div>
  )
}
