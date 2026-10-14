import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { listRoadmaps } from '../api/catalogue'
import { getStreak, listEnrollments } from '../api/learning'
import type { Enrollment, RoadmapSummary, Streak } from '../api/types'
import { useAuth } from '../auth/authStore'
import { AsyncSection } from '../components/AsyncSection'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { IconBook, IconFlame, IconMedal } from '../components/icons'
import { IllustrationEmpty } from '../components/illustrations'
import { ProgressRing } from '../components/ProgressRing'
import { ResumeBanner } from '../components/ResumeBanner'
import { TrackBadge } from '../components/TrackBadge'
import { TrackCover } from '../components/TrackCover'
import { useGamification } from '../gamification/gamificationStore'
import { streakStatus } from '../gamification/streak'
import { useAsync } from '../hooks/useAsync'
import styles from './DashboardPage.module.css'

interface DashboardData {
  enrollments: Enrollment[]
  roadmaps: RoadmapSummary[]
  streak: Streak
}

async function loadDashboard(): Promise<DashboardData> {
  const [enrollments, roadmaps, streak] = await Promise.all([
    listEnrollments(),
    listRoadmaps(),
    getStreak(),
  ])
  return { enrollments, roadmaps, streak }
}

export function DashboardPage() {
  const navigate = useNavigate()
  const user = useAuth((s) => s.user)
  const state = useAsync(loadDashboard, [])

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Bonjour {user?.displayName}</h1>
      <ResumeBanner />

      <AsyncSection state={state}>
        {({ enrollments, roadmaps, streak }) => (
          <DashboardView
            enrollments={enrollments}
            roadmaps={roadmaps}
            streak={streak}
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
  streak: Streak
  onGo: (path: string) => void
}

function DashboardView({ enrollments, roadmaps, streak, onGo }: ViewProps) {
  const roadmapById = useMemo(() => {
    const map = new Map<string, RoadmapSummary>()
    roadmaps.forEach((r) => map.set(r.id, r))
    return map
  }, [roadmaps])

  const enrolledIds = useMemo(() => new Set(enrollments.map((e) => e.roadmapId)), [enrollments])
  const suggestions = roadmaps.filter((r) => !enrolledIds.has(r.id)).slice(0, 3)

  const profile = useGamification((s) => s.profile)
  const activeCount = enrollments.filter((e) => e.progressPercent < 100).length
  const doneCount = enrollments.filter((e) => e.progressPercent >= 100).length
  const status = streakStatus(streak)
  const nudge =
    status === 'done-today'
      ? 'Objectif du jour atteint, série sécurisée.'
      : status === 'at-risk'
        ? 'Encore une leçon aujourd’hui pour garder ta série.'
        : 'Termine une leçon aujourd’hui pour lancer ta série.'

  return (
    <>
      <div className={`${styles.stats} sf-stagger`}>
        <div className={styles.tile}>
          <span className={styles.tileIcon} data-tone="flame" aria-hidden="true">
            <IconFlame size={20} />
          </span>
          <div className={styles.tileBody}>
            <div className={styles.tileValue}>
              {streak.currentLength} jour{streak.currentLength > 1 ? 's' : ''}
            </div>
            <div className={styles.tileLabel}>Série (record : {streak.longestLength})</div>
          </div>
        </div>
        <div className={styles.tile}>
          <span className={styles.tileIcon} data-tone="medal" aria-hidden="true">
            <IconMedal size={20} />
          </span>
          <div className={styles.tileBody}>
            <div className={styles.tileValue}>{profile?.currentLevel?.name ?? 'Débutant'}</div>
            <div className={styles.tileLabel}>{profile?.totalXp ?? 0} XP</div>
          </div>
        </div>
        <div className={styles.tile}>
          <span className={styles.tileIcon} data-tone="book" aria-hidden="true">
            <IconBook size={20} />
          </span>
          <div className={styles.tileBody}>
            <div className={styles.tileValue}>{activeCount}</div>
            <div className={styles.tileLabel}>Parcours en cours</div>
          </div>
        </div>
        <div className={styles.tile}>
          <span className={styles.tileIcon} data-tone="check" aria-hidden="true">
            <IconMedal size={20} />
          </span>
          <div className={styles.tileBody}>
            <div className={styles.tileValue}>{doneCount}</div>
            <div className={styles.tileLabel}>Parcours terminés</div>
          </div>
        </div>
      </div>
      <p className={styles.nudge}>{nudge}</p>

      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>Vos parcours</h2>
        {enrollments.length === 0 ? (
          <Card title="Bienvenue, commence ici">
            <div className={styles.emptyWrap}>
              <IllustrationEmpty width={200} />
              <div>
                <p className={styles.empty}>
                  Voici comment ça marche : choisis un parcours dans le catalogue, avance leçon par
                  leçon à ton rythme, et valide chaque étape par un quiz et des défis. Ta progression
                  est enregistrée, tu peux reprendre exactement où tu t'arrêtes.
                </p>
                <ol className={styles.steps}>
                  <li>Choisis un parcours qui t'intéresse.</li>
                  <li>Suis les leçons dans l'ordre conseillé.</li>
                  <li>Teste tes acquis avec les quiz et les défis.</li>
                </ol>
                <Button onClick={() => onGo('/catalogue')}>Explorer le catalogue</Button>
              </div>
            </div>
          </Card>
        ) : (
          <div className={`${styles.grid} sf-stagger`}>
            {enrollments.map((enrollment) => {
              const roadmap = roadmapById.get(enrollment.roadmapId)
              const target = enrollment.lastActiveLessonId
                ? `/lessons/${enrollment.lastActiveLessonId}`
                : roadmap
                  ? `/roadmaps/${roadmap.slug}`
                  : '/catalogue'
              const done = enrollment.progressPercent >= 100
              return (
                <Card
                  key={enrollment.id}
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
                    <span className={styles.cardProgressLabel}>
                      {done ? 'Parcours terminé' : 'Progression'}
                    </span>
                  </div>
                </Card>
              )
            })}
          </div>
        )}
      </section>

      {suggestions.length > 0 && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>À découvrir</h2>
          <div className={`${styles.grid} sf-stagger`}>
            {suggestions.map((roadmap) => (
              <Card
                key={roadmap.id}
                className="sf-lift"
                media={<TrackCover slug={roadmap.slug} />}
                title={
                  <span className={styles.cardTitle}>
                    <TrackBadge slug={roadmap.slug} title={roadmap.title} size={28} />
                    <span>{roadmap.title}</span>
                  </span>
                }
                footer={
                  <Button variant="secondary" size="sm" onClick={() => onGo(`/roadmaps/${roadmap.slug}`)}>
                    Découvrir
                  </Button>
                }
              >
                <p className={styles.summary}>{roadmap.summary}</p>
              </Card>
            ))}
          </div>
        </section>
      )}
    </>
  )
}
