import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { listRoadmaps } from '../api/catalogue'
import { listEnrollments } from '../api/learning'
import type { Enrollment, RoadmapSummary } from '../api/types'
import { AsyncSection } from '../components/AsyncSection'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { IllustrationEmpty } from '../components/illustrations'
import { ProgressRing } from '../components/ProgressRing'
import { TrackBadge } from '../components/TrackBadge'
import { TrackCover } from '../components/TrackCover'
import { useAsync } from '../hooks/useAsync'
import styles from './MyPathsPage.module.css'

interface MyPathsData {
  enrollments: Enrollment[]
  roadmaps: RoadmapSummary[]
}

async function loadMyPaths(): Promise<MyPathsData> {
  const [enrollments, roadmaps] = await Promise.all([listEnrollments(), listRoadmaps()])
  return { enrollments, roadmaps }
}

/** Hub « Mes parcours » : parcours en cours (reprise) et parcours terminés (certificat). */
export function MyPathsPage() {
  const navigate = useNavigate()
  const state = useAsync(loadMyPaths, [])

  return (
    <div className={styles.page}>
      <header className={styles.head}>
        <h1 className={styles.title}>Mes parcours</h1>
        <p className={styles.subtitle}>Reprends là où tu t'es arrêté, ou revois ce que tu as terminé.</p>
      </header>

      <AsyncSection state={state}>
        {({ enrollments, roadmaps }) => (
          <MyPathsView
            enrollments={enrollments}
            roadmaps={roadmaps}
            onGo={(path) => navigate(path)}
          />
        )}
      </AsyncSection>
    </div>
  )
}

interface ViewProps {
  enrollments: Enrollment[]
  roadmaps: RoadmapSummary[]
  onGo: (path: string) => void
}

function MyPathsView({ enrollments, roadmaps, onGo }: ViewProps) {
  const roadmapById = useMemo(() => {
    const map = new Map<string, RoadmapSummary>()
    roadmaps.forEach((r) => map.set(r.id, r))
    return map
  }, [roadmaps])

  const inProgress = enrollments.filter((e) => e.progressPercent < 100)
  const completed = enrollments.filter((e) => e.progressPercent >= 100)

  if (enrollments.length === 0) {
    return (
      <Card title="Aucun parcours pour l'instant">
        <div className={styles.emptyWrap}>
          <IllustrationEmpty width={200} />
          <div>
            <p className={styles.empty}>
              Tu n'es inscrit à aucun parcours. Choisis-en un dans le catalogue, avance leçon par
              leçon à ton rythme : ta progression sera enregistrée ici, prête à reprendre.
            </p>
            <Button onClick={() => onGo('/catalogue')}>Explorer le catalogue</Button>
          </div>
        </div>
      </Card>
    )
  }

  return (
    <>
      {inProgress.length > 0 && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>En cours</h2>
          <div className={`${styles.grid} sf-stagger`}>
            {inProgress.map((enrollment) => (
              <PathCard
                key={enrollment.id}
                enrollment={enrollment}
                roadmap={roadmapById.get(enrollment.roadmapId)}
                onGo={onGo}
              />
            ))}
          </div>
        </section>
      )}

      {completed.length > 0 && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>Terminés</h2>
          <div className={`${styles.grid} sf-stagger`}>
            {completed.map((enrollment) => (
              <PathCard
                key={enrollment.id}
                enrollment={enrollment}
                roadmap={roadmapById.get(enrollment.roadmapId)}
                onGo={onGo}
              />
            ))}
          </div>
        </section>
      )}
    </>
  )
}

interface PathCardProps {
  enrollment: Enrollment
  roadmap: RoadmapSummary | undefined
  onGo: (path: string) => void
}

function PathCard({ enrollment, roadmap, onGo }: PathCardProps) {
  const done = enrollment.progressPercent >= 100
  const target = enrollment.lastActiveLessonId
    ? `/lessons/${enrollment.lastActiveLessonId}`
    : roadmap
      ? `/roadmaps/${roadmap.slug}`
      : '/catalogue'

  return (
    <Card
      className="sf-lift"
      media={roadmap && <TrackCover slug={roadmap.slug} />}
      title={
        <span className={styles.cardTitle}>
          {roadmap && <TrackBadge slug={roadmap.slug} title={roadmap.title} size={28} />}
          <span>{roadmap?.title ?? 'Parcours'}</span>
        </span>
      }
      footer={
        done && roadmap ? (
          <Button size="sm" onClick={() => onGo(`/certificate/${roadmap.slug}`)}>
            Voir mon certificat
          </Button>
        ) : (
          <Button size="sm" onClick={() => onGo(target)}>
            {enrollment.progressPercent > 0 ? 'Continuer' : 'Commencer'}
          </Button>
        )
      }
    >
      <div className={styles.cardProgress}>
        <ProgressRing value={enrollment.progressPercent} size={64} />
        <span className={styles.cardProgressLabel}>{done ? 'Parcours terminé' : 'Progression'}</span>
      </div>
    </Card>
  )
}
