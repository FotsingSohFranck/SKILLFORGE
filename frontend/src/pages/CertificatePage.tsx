import { useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getRoadmap } from '../api/catalogue'
import { getRoadmapProgress } from '../api/learning'
import { ApiError, type RoadmapGraph } from '../api/types'
import { useAuth } from '../auth/authStore'
import { AsyncSection } from '../components/AsyncSection'
import { Button } from '../components/Button'
import { celebrate } from '../celebrate'
import { trackMeta } from '../trackMeta'
import { useAsync } from '../hooks/useAsync'
import styles from './CertificatePage.module.css'

interface CertData {
  graph: RoadmapGraph
  percent: number
}

async function load(slug: string): Promise<CertData> {
  const graph = await getRoadmap(slug)
  let percent = 0
  try {
    const progress = await getRoadmapProgress(graph.roadmap.id)
    percent = progress.enrollment.progressPercent
  } catch (err) {
    if (!(err instanceof ApiError && err.code === 'NOT_ENROLLED')) throw err
  }
  return { graph, percent }
}

/** Identifiant de vérification court et déterministe (email + parcours). */
function certificateId(email: string, slug: string): string {
  let h = 0
  for (const ch of `${email}|${slug}`) h = (h * 31 + ch.charCodeAt(0)) >>> 0
  const code = h.toString(36).toUpperCase().padStart(6, '0').slice(0, 6)
  return `SF-${slug.replace(/[^a-z0-9]/gi, '').slice(0, 4).toUpperCase()}-${code}`
}

/** Sceau / médaillon décoratif (SVG), teinté à la couleur du parcours. */
function Seal({ accent }: { accent: string }) {
  return (
    <svg className={styles.seal} viewBox="0 0 120 150" role="img" aria-label="Sceau de réussite">
      {/* Rubans */}
      <path d="M44 92 L34 140 L52 128 L60 142 L60 92 Z" fill={accent} opacity="0.85" />
      <path d="M76 92 L86 140 L68 128 L60 142 L60 92 Z" fill={accent} opacity="0.6" />
      {/* Médaillon */}
      <circle cx="60" cy="56" r="46" fill="#fff" stroke={accent} strokeWidth="3" />
      <circle cx="60" cy="56" r="39" fill="none" stroke={accent} strokeWidth="1.5" opacity="0.6" />
      {/* Étoile */}
      <path
        d="M60 32 l6.9 14 15.4 2.2 -11.1 10.9 2.6 15.3 -13.8 -7.2 -13.8 7.2 2.6 -15.3 -11.1 -10.9 15.4 -2.2 Z"
        fill={accent}
      />
    </svg>
  )
}

/** Certificat de fin de parcours premium (généré côté client à 100 %, imprimable). */
export function CertificatePage() {
  const { slug = '' } = useParams()
  const user = useAuth((s) => s.user)
  const state = useAsync(() => load(slug), [slug])
  const today = new Date().toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric' })

  // Célébration à l'affichage du certificat (parcours terminé).
  useEffect(() => {
    if (state.data && state.data.percent >= 100) celebrate()
  }, [state.data])

  return (
    <div className={styles.page}>
      <AsyncSection state={state}>
        {({ graph, percent }) => {
          if (percent < 100) {
            return (
              <div className={styles.locked}>
                <h1>Certificat indisponible</h1>
                <p>
                  Termine le parcours « {graph.roadmap.title} » à 100 % pour débloquer ton certificat.
                  Progression actuelle : {Math.round(percent)} %.
                </p>
                <Link to={`/roadmaps/${graph.roadmap.slug}`}>
                  <Button>Retour au parcours</Button>
                </Link>
              </div>
            )
          }
          const { accent } = trackMeta(graph.roadmap.slug, graph.roadmap.title)
          const id = certificateId(user?.email ?? 'anonymous', graph.roadmap.slug)
          const style = { '--cert-accent': accent } as React.CSSProperties

          return (
            <>
              <div className={styles.toolbar}>
                <Link to={`/roadmaps/${graph.roadmap.slug}`} className={styles.back}>
                  ← Retour au parcours
                </Link>
                <Button onClick={() => window.print()}>Télécharger / Imprimer</Button>
              </div>

              <div className={styles.sheet} style={style}>
                <div className={styles.frame}>
                  <div className={styles.watermark} aria-hidden="true">
                    SkillForge
                  </div>

                  <div className={styles.brand}>SkillForge</div>

                  <Seal accent={accent} />

                  <div className={styles.kicker}>Certificat de réussite</div>

                  <div className={styles.rule} aria-hidden="true">
                    <span />
                    <i />
                    <span />
                  </div>

                  <div className={styles.awarded}>Ce certificat est fièrement décerné à</div>
                  <div className={styles.name}>{user?.displayName ?? 'Apprenant'}</div>

                  <div className={styles.for}>pour avoir mené à bien l'intégralité du parcours</div>
                  <div className={styles.track}>{graph.roadmap.title}</div>

                  <div className={styles.footerRow}>
                    <div className={styles.block}>
                      <div className={styles.blockValue}>{today}</div>
                      <div className={styles.blockLabel}>Date de délivrance</div>
                    </div>
                    <div className={styles.block}>
                      <div className={styles.blockValue}>{graph.nodes.length}</div>
                      <div className={styles.blockLabel}>Nœuds complétés</div>
                    </div>
                    <div className={styles.block}>
                      <div className={styles.signature}>SkillForge</div>
                      <div className={styles.blockLabel}>Plateforme d'apprentissage</div>
                    </div>
                  </div>

                  <div className={styles.verify}>Identifiant de vérification : {id}</div>
                </div>
              </div>
            </>
          )
        }}
      </AsyncSection>
    </div>
  )
}
