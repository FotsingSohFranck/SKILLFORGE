import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getRoadmap, listRoadmaps } from '../api/catalogue'
import { enroll, listEnrollments } from '../api/learning'
import { ApiError, type Enrollment, type RoadmapSummary } from '../api/types'
import { AsyncSection } from '../components/AsyncSection'
import { Badge } from '../components/Badge'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { IconBook, IconClock } from '../components/icons'
import { IllustrationEmpty } from '../components/illustrations'
import { Input } from '../components/Input'
import { TrackBadge } from '../components/TrackBadge'
import { TrackCover } from '../components/TrackCover'
import { trackCategory } from '../trackMeta'
import { useToasts } from '../components/toast/toastStore'
import { useAsync } from '../hooks/useAsync'
import styles from './CataloguePage.module.css'

interface NodeMeta {
  nodes: number
  minutes: number
}

interface CatalogueData {
  roadmaps: RoadmapSummary[]
  enrollmentByRoadmap: Map<string, Enrollment>
  metaByRoadmap: Map<string, NodeMeta>
}

async function loadCatalogue(query: string): Promise<CatalogueData> {
  const [roadmaps, enrollments] = await Promise.all([listRoadmaps(query), listEnrollments().catch(() => [])])
  const enrollmentByRoadmap = new Map(enrollments.map((e) => [e.roadmapId, e]))
  // Méta (nœuds + durée) : on charge les graphes en parallèle, best-effort.
  const graphs = await Promise.all(roadmaps.map((r) => getRoadmap(r.slug).catch(() => null)))
  const metaByRoadmap = new Map<string, NodeMeta>()
  graphs.forEach((g) => {
    if (!g) return
    const minutes = g.nodes.reduce((sum, n) => sum + (n.estimatedMinutes ?? 0), 0)
    metaByRoadmap.set(g.roadmap.id, { nodes: g.nodes.length, minutes })
  })
  return { roadmaps, enrollmentByRoadmap, metaByRoadmap }
}

function formatDuration(minutes: number): string {
  if (minutes <= 0) return ''
  if (minutes < 60) return `${minutes} min`
  const h = Math.round(minutes / 60)
  return `~${h} h`
}

type Filter = 'all' | 'track' | 'cert' | 'inprogress' | 'done'

const FILTERS: { key: Filter; label: string }[] = [
  { key: 'all', label: 'Tous' },
  { key: 'track', label: 'Parcours' },
  { key: 'cert', label: 'Certifications' },
  { key: 'inprogress', label: 'En cours' },
  { key: 'done', label: 'Terminés' },
]

export function CataloguePage() {
  const navigate = useNavigate()
  const push = useToasts((s) => s.push)
  const [term, setTerm] = useState('')
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState<Filter>('all')
  const [enrollingId, setEnrollingId] = useState<string | null>(null)

  const state = useAsync(() => loadCatalogue(query), [query])

  // Recherche instantanée (debounce) : plus besoin de bouton.
  useEffect(() => {
    const t = setTimeout(() => setQuery(term.trim()), 300)
    return () => clearTimeout(t)
  }, [term])

  async function start(roadmap: RoadmapSummary) {
    setEnrollingId(roadmap.id)
    try {
      await enroll(roadmap.id)
      navigate(`/roadmaps/${roadmap.slug}`)
    } catch (err) {
      push('danger', err instanceof ApiError ? err.message : "L'inscription a échoué.")
    } finally {
      setEnrollingId(null)
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Catalogue</h1>

      <div className={styles.toolbar}>
        <Input
          label="Rechercher un parcours"
          placeholder="Backend, Docker, Kubernetes…"
          value={term}
          onChange={(e) => setTerm(e.target.value)}
          className={styles.searchField}
        />
        <div className={styles.filters} role="tablist" aria-label="Filtres">
          {FILTERS.map((f) => (
            <button
              key={f.key}
              type="button"
              role="tab"
              aria-selected={filter === f.key}
              className={[styles.chip, 'sf-pressable', filter === f.key ? styles.chipActive : ''].filter(Boolean).join(' ')}
              onClick={() => setFilter(f.key)}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      <AsyncSection
        state={state}
        empty={
          <div className={styles.emptyState}>
            <IllustrationEmpty width={200} />
            <p className={styles.empty}>Aucun parcours ne correspond à votre recherche.</p>
          </div>
        }
      >
        {({ roadmaps, enrollmentByRoadmap, metaByRoadmap }) => {
          const filtered = roadmaps.filter((r) => {
            const e = enrollmentByRoadmap.get(r.id)
            switch (filter) {
              case 'track':
                return r.kind !== 'CERTIFICATION'
              case 'cert':
                return r.kind === 'CERTIFICATION'
              case 'inprogress':
                return e != null && e.progressPercent > 0 && e.progressPercent < 100
              case 'done':
                return e != null && e.progressPercent >= 100
              default:
                return true
            }
          })

          if (filtered.length === 0) {
            return <p className={styles.empty}>Aucun parcours dans cette catégorie.</p>
          }

          // Regroupement par thème.
          const groups = new Map<string, RoadmapSummary[]>()
          filtered.forEach((r) => {
            const cat = trackCategory(r.slug, r.kind)
            const list = groups.get(cat) ?? []
            list.push(r)
            groups.set(cat, list)
          })

          return (
            <div className={styles.groups}>
              {[...groups.entries()].map(([category, list]) => (
                <section key={category} className={styles.group}>
                  <h2 className={styles.groupTitle}>{category}</h2>
                  <div className={`${styles.grid} sf-stagger`}>
                    {list.map((roadmap) => {
                      const e = enrollmentByRoadmap.get(roadmap.id)
                      const meta = metaByRoadmap.get(roadmap.id)
                      const done = e != null && e.progressPercent >= 100
                      const inProgress = e != null && e.progressPercent > 0 && !done
                      const continueTo = e?.lastActiveLessonId
                        ? `/lessons/${e.lastActiveLessonId}`
                        : `/roadmaps/${roadmap.slug}`
                      return (
                        <Card
                          key={roadmap.id}
                          className="sf-lift"
                          media={<TrackCover slug={roadmap.slug} />}
                          title={
                            <div className={styles.cardHead}>
                              <TrackBadge slug={roadmap.slug} title={roadmap.title} size={32} />
                              <span className={styles.cardTitleText}>{roadmap.title}</span>
                              {roadmap.kind === 'CERTIFICATION' && (
                                <Badge tone="secondary">Certif</Badge>
                              )}
                            </div>
                          }
                          footer={
                            <div className={styles.actions}>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => navigate(`/roadmaps/${roadmap.slug}`)}
                              >
                                Aperçu
                              </Button>
                              {done ? (
                                <Button size="sm" onClick={() => navigate(`/roadmaps/${roadmap.slug}`)}>
                                  Revoir
                                </Button>
                              ) : inProgress ? (
                                <Button size="sm" onClick={() => navigate(continueTo)}>
                                  Continuer
                                </Button>
                              ) : (
                                <Button
                                  size="sm"
                                  disabled={enrollingId === roadmap.id}
                                  onClick={() => void start(roadmap)}
                                >
                                  {enrollingId === roadmap.id ? 'Inscription…' : 'Commencer'}
                                </Button>
                              )}
                            </div>
                          }
                        >
                          <p className={styles.summary}>{roadmap.summary}</p>
                          <div className={styles.meta}>
                            {meta && meta.nodes > 0 && (
                              <span className={styles.metaItem}>
                                <IconBook size={14} /> {meta.nodes} nœuds
                              </span>
                            )}
                            {meta && meta.minutes > 0 && (
                              <span className={styles.metaItem}>
                                <IconClock size={14} /> {formatDuration(meta.minutes)}
                              </span>
                            )}
                            {done && <Badge tone="success">Terminé</Badge>}
                            {inProgress && (
                              <Badge tone="warning">{Math.round(e!.progressPercent)} %</Badge>
                            )}
                          </div>
                        </Card>
                      )
                    })}
                  </div>
                </section>
              ))}
            </div>
          )
        }}
      </AsyncSection>
    </div>
  )
}
