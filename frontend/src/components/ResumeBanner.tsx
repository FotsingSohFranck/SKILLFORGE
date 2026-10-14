import { useNavigate } from 'react-router-dom'
import { getLesson, getNode, listRoadmaps } from '../api/catalogue'
import { getResume, getRoadmapProgress } from '../api/learning'
import { useAsync } from '../hooks/useAsync'
import { Button } from './Button'
import { TrackBadge } from './TrackBadge'
import { deEmDash } from '../format'
import styles from './ResumeBanner.module.css'

interface ResumeContext {
  lessonId: string
  nodeTitle: string | null
  lessonTitle: string | null
  trackSlug: string | null
  trackTitle: string | null
  percent: number | null
}

/** Charge le point de reprise puis, best-effort, les titres, le parcours et la progression. */
async function loadResume(): Promise<ResumeContext | null> {
  const resume = await getResume()
  if (!resume) return null
  const [lesson, node, roadmaps, progress] = await Promise.all([
    getLesson(resume.lessonId).catch(() => null),
    getNode(resume.nodeId).catch(() => null),
    listRoadmaps().catch(() => []),
    getRoadmapProgress(resume.roadmapId).catch(() => null),
  ])
  const track = roadmaps.find((r) => r.id === resume.roadmapId) ?? null
  return {
    lessonId: resume.lessonId,
    nodeTitle: node?.title ?? null,
    lessonTitle: lesson?.lesson.title ?? null,
    trackSlug: track?.slug ?? null,
    trackTitle: track?.title ?? null,
    percent: progress?.enrollment.progressPercent ?? null,
  }
}

/** Bandeau « Reprendre » : mène à la dernière leçon active (point de reprise serveur). */
export function ResumeBanner() {
  const navigate = useNavigate()
  const { data, loading } = useAsync(loadResume, [])

  if (loading || !data) return null

  return (
    <div className={styles.banner}>
      {data.trackSlug && data.trackTitle ? (
        <TrackBadge slug={data.trackSlug} title={data.trackTitle} size={48} />
      ) : (
        <span className={styles.icon} aria-hidden="true" />
      )}
      <div className={styles.body}>
        <div className={styles.eyebrow}>Reprendre là où tu t'es arrêté</div>
        <div className={styles.lesson}>
          {data.lessonTitle ? deEmDash(data.lessonTitle) : 'Ta dernière leçon active'}
        </div>
        <div className={styles.meta}>
          {data.nodeTitle && <span>{deEmDash(data.nodeTitle)}</span>}
          {data.percent != null && (
            <span className={styles.percent}>{Math.round(data.percent)} % du parcours</span>
          )}
        </div>
      </div>
      <Button onClick={() => navigate(`/lessons/${data.lessonId}`)}>Reprendre</Button>
    </div>
  )
}
