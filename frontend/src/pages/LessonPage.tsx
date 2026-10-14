import { useEffect, useMemo, useRef, useState } from 'react'
import type { ReactNode, RefObject } from 'react'
import { NavLink, useNavigate, useParams } from 'react-router-dom'
import { getLesson, getNode, listNodeLessons } from '../api/catalogue'
import {
  getNodeLessonProgress,
  listBookmarks,
  markCompleted,
  markViewed,
  toggleBookmark,
} from '../api/learning'
import { ApiError, type LessonDetail, type RoadmapNode, type Section } from '../api/types'
import { AsyncSection } from '../components/AsyncSection'
import { Badge } from '../components/Badge'
import { Button } from '../components/Button'
import { Callout } from '../components/Callout'
import { CodeBlock } from '../components/CodeBlock'
import {
  IconCheck,
  IconCheckCircle,
  IconCircle,
  IconClock,
  IconCross,
  IconInsight,
  IconStar,
  IconStarOutline,
  IconWarning,
} from '../components/icons'
import { LessonNotes } from '../components/LessonNotes'
import { MermaidDiagram } from '../components/MermaidDiagram'
import { QuizRunner } from '../components/QuizRunner'
import { celebrate } from '../celebrate'
import { deEmDash } from '../format'
import { useGamification } from '../gamification/gamificationStore'
import { useToasts } from '../components/toast/toastStore'
import { useAsync } from '../hooks/useAsync'
import styles from './LessonPage.module.css'

interface LessonData {
  detail: LessonDetail
  node: RoadmapNode
  siblings: { id: string; position: number; title: string }[]
  completedIds: string[]
  bookmarked: boolean
}

async function loadLesson(id: string): Promise<LessonData> {
  const detail = await getLesson(id)
  const nodeId = detail.lesson.nodeId
  const [node, siblings, progress] = await Promise.all([
    getNode(nodeId),
    listNodeLessons(nodeId),
    getNodeLessonProgress(nodeId),
  ])
  let bookmarked = false
  try {
    bookmarked = (await listBookmarks()).some((b) => b.targetType === 'LESSON' && b.targetId === id)
  } catch {
    // favoris secondaires
  }
  return { detail, node, siblings, completedIds: progress.completedLessonIds, bookmarked }
}

/** Métadonnées d'affichage par type de section (étiquette + variante visuelle). */
const SECTION_KIND: Record<string, { label: string; kind: string }> = {
  CONCEPT: { label: 'Notion', kind: 'concept' },
  TEXT: { label: 'Notion', kind: 'concept' },
  CODE: { label: 'Code', kind: 'code' },
  DIAGRAM: { label: 'Schéma', kind: 'diagram' },
  EXERCISE: { label: 'Exercice', kind: 'exercise' },
  QUIZ: { label: 'Quiz', kind: 'quiz' },
  CALLOUT: { label: 'À retenir', kind: 'callout' },
  IMAGE: { label: 'Illustration', kind: 'image' },
}

function sectionKind(type: string) {
  return SECTION_KIND[type] ?? { label: 'Notion', kind: 'concept' }
}

/** Estimation du temps de lecture : ~180 mots/min sur l'ensemble des sections. */
function readingMinutes(sections: Section[]): number {
  let words = 0
  for (const s of sections) {
    const text = `${s.body ?? ''} ${s.solution ?? ''}`
    words += text.split(/\s+/).filter(Boolean).length
  }
  return Math.max(1, Math.round(words / 180))
}

/** Progression de lecture (0..100) de l'article, basée sur le défilement de la fenêtre. */
function useReadingProgress(ref: RefObject<HTMLElement | null>): number {
  const [progress, setProgress] = useState(0)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    let raf = 0
    const onScroll = () => {
      cancelAnimationFrame(raf)
      raf = requestAnimationFrame(() => {
        const rect = el.getBoundingClientRect()
        const total = el.offsetHeight - window.innerHeight
        const scrolled = Math.min(Math.max(-rect.top, 0), Math.max(total, 1))
        setProgress(total > 0 ? Math.min(100, (scrolled / total) * 100) : 100)
      })
    }
    window.addEventListener('scroll', onScroll, { passive: true })
    window.addEventListener('resize', onScroll)
    onScroll()
    return () => {
      window.removeEventListener('scroll', onScroll)
      window.removeEventListener('resize', onScroll)
      cancelAnimationFrame(raf)
    }
  }, [ref])
  return progress
}

/** Section active (pour surligner le sommaire), via IntersectionObserver. */
function useActiveSection(ids: string[]): string {
  const [active, setActive] = useState(ids[0] ?? '')
  const key = ids.join('|')
  useEffect(() => {
    if (!ids.length) return
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((e) => e.isIntersecting)
          .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
        if (visible[0]) setActive(visible[0].target.id)
      },
      { rootMargin: '-15% 0px -70% 0px', threshold: 0 },
    )
    ids.forEach((id) => {
      const el = document.getElementById(id)
      if (el) observer.observe(el)
    })
    return () => observer.disconnect()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])
  return active
}

export function LessonPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const push = useToasts((s) => s.push)
  const celebrateGamif = useGamification((s) => s.celebrateAfterActivity)
  const state = useAsync(() => loadLesson(id), [id])
  const [completed, setCompleted] = useState(false)
  const [favorite, setFavorite] = useState(false)
  const [saving, setSaving] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const articleRef = useRef<HTMLElement | null>(null)

  const data = state.data

  // Reprise EXACTE : marquer la leçon comme vue, et restaurer la position de lecture.
  useEffect(() => {
    if (!data) return
    setCompleted(data.completedIds.includes(id))
    setFavorite(data.bookmarked)
    setDrawerOpen(false)
    void markViewed({
      roadmapId: data.node.roadmapId,
      nodeId: data.node.id,
      lessonId: id,
    }).catch(() => undefined)
    // Reprend la position de défilement sauvegardée pour cette leçon (sinon en haut).
    let saved = 0
    try {
      saved = Number(sessionStorage.getItem(`lesson-scroll-${id}`) ?? '0')
    } catch {
      saved = 0
    }
    requestAnimationFrame(() => window.scrollTo({ top: saved }))
  }, [data, id])

  // Mémorise la position de défilement de la leçon (reprise au bon endroit).
  useEffect(() => {
    if (!data) return
    let raf = 0
    const onScroll = () => {
      cancelAnimationFrame(raf)
      raf = requestAnimationFrame(() => {
        try {
          sessionStorage.setItem(`lesson-scroll-${id}`, String(Math.round(window.scrollY)))
        } catch {
          // sans conséquence
        }
      })
    }
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => {
      window.removeEventListener('scroll', onScroll)
      cancelAnimationFrame(raf)
    }
  }, [data, id])

  const siblingsOrdered = useMemo(
    () => (data ? [...data.siblings].sort((a, b) => a.position - b.position) : []),
    [data],
  )
  const index = siblingsOrdered.findIndex((l) => l.id === id)
  const prev = index > 0 ? siblingsOrdered[index - 1] : null
  const next = index >= 0 && index < siblingsOrdered.length - 1 ? siblingsOrdered[index + 1] : null

  const sections = useMemo(
    () => (data ? [...data.detail.sections].sort((a, b) => a.position - b.position) : []),
    [data],
  )
  const tocItems = useMemo(
    () =>
      sections
        // Les images et callouts ne sont pas des titres de navigation -> hors sommaire.
        .filter((s) => s.title && s.type !== 'IMAGE' && s.type !== 'CALLOUT')
        .map((s) => ({ id: `sec-${s.id}`, title: s.title as string })),
    [sections],
  )
  const tocIds = useMemo(() => tocItems.map((t) => t.id), [tocItems])
  const activeSection = useActiveSection(tocIds)
  const readProgress = useReadingProgress(articleRef)

  async function complete() {
    if (!data) return
    setSaving(true)
    try {
      await markCompleted({ roadmapId: data.node.roadmapId, nodeId: data.node.id, lessonId: id })
      setCompleted(true)
      push('success', 'Leçon terminée')
      // Célébration si cette leçon termine le nœud entier.
      const nowDone = new Set(data.completedIds)
      nowDone.add(id)
      if (siblingsOrdered.length > 0 && siblingsOrdered.every((s) => nowDone.has(s.id))) {
        push('success', 'Nœud terminé, bravo !')
        celebrate()
      }
      void celebrateGamif()
    } catch (err) {
      push('danger', err instanceof ApiError ? err.message : 'Action impossible.')
    } finally {
      setSaving(false)
    }
  }

  async function toggleFavori() {
    const previous = favorite
    setFavorite(!previous)
    try {
      const { bookmarked } = await toggleBookmark('LESSON', id)
      setFavorite(bookmarked)
    } catch (err) {
      setFavorite(previous)
      push('danger', err instanceof ApiError ? err.message : 'Action impossible.')
    }
  }

  function goToSection(anchor: string) {
    document.getElementById(anchor)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div className={styles.page}>
      <AsyncSection state={state}>
        {({ detail, node }) => {
          const completedSet = new Set(data?.completedIds ?? [])
          if (completed) completedSet.add(id)
          const doneCount = siblingsOrdered.filter((s) => completedSet.has(s.id)).length
          const total = siblingsOrdered.length || 1
          const nodePercent = Math.round((doneCount / total) * 100)
          const minutes = readingMinutes(sections)

          const railInner = (
            <>
              <NavLink to={`/nodes/${node.id}`} className={styles.railNode}>
                {deEmDash(node.title)}
              </NavLink>
              <div className={styles.railProgress}>
                <div className={styles.railBar}>
                  <span style={{ width: `${nodePercent}%` }} />
                </div>
                <span className={styles.railCount}>
                  {doneCount} / {siblingsOrdered.length} leçons
                </span>
              </div>
              <ol className={styles.railList}>
                {siblingsOrdered.map((sib, i) => {
                  const done = completedSet.has(sib.id)
                  const current = sib.id === id
                  return (
                    <li key={sib.id}>
                      <NavLink
                        to={`/lessons/${sib.id}`}
                        onClick={() => setDrawerOpen(false)}
                        className={[styles.railItem, 'sf-pressable', current ? styles.railCurrent : '']
                          .filter(Boolean)
                          .join(' ')}
                        aria-current={current ? 'page' : undefined}
                      >
                        <span className={styles.railCheck} aria-hidden="true">
                          {done ? <IconCheckCircle size={16} /> : <IconCircle size={16} />}
                        </span>
                        <span className={styles.railIndex}>{i + 1}.</span>
                        <span className={styles.railTitle}>{deEmDash(sib.title)}</span>
                      </NavLink>
                    </li>
                  )
                })}
              </ol>
            </>
          )

          return (
            <div className={styles.layout}>
              {/* --- Rail de curriculum (leçons du nœud), masqué sur mobile --- */}
              <aside className={styles.rail} aria-label="Leçons du nœud">
                {railInner}
              </aside>

              {/* --- Tiroir mobile du curriculum --- */}
              {drawerOpen && (
                <div
                  className={styles.drawerOverlay}
                  role="presentation"
                  onClick={() => setDrawerOpen(false)}
                >
                  <aside
                    className={styles.drawer}
                    aria-label="Leçons du nœud"
                    onClick={(e) => e.stopPropagation()}
                  >
                    {railInner}
                  </aside>
                </div>
              )}

              {/* --- Colonne centrale (article) --- */}
              <div className={styles.center}>
                <div className={styles.readbar} aria-hidden="true">
                  <span style={{ width: `${readProgress}%` }} />
                </div>

                <button
                  type="button"
                  className={styles.drawerToggle}
                  onClick={() => setDrawerOpen(true)}
                >
                  Leçons ({doneCount}/{siblingsOrdered.length})
                </button>

                <nav className={styles.breadcrumb} aria-label="Fil d'Ariane">
                  <NavLink to={`/nodes/${node.id}`}>{deEmDash(node.title)}</NavLink>
                  <span className={styles.crumbSep} aria-hidden="true">
                    /
                  </span>
                  <span className={styles.crumbCurrent}>{deEmDash(detail.lesson.title)}</span>
                </nav>

                <header className={styles.header}>
                  <div className={styles.titleRow}>
                    <h1 className={styles.title}>{deEmDash(detail.lesson.title)}</h1>
                    <button
                      type="button"
                      className={styles.star}
                      aria-pressed={favorite}
                      aria-label={favorite ? 'Retirer des favoris' : 'Ajouter aux favoris'}
                      onClick={() => void toggleFavori()}
                    >
                      {favorite ? <IconStar size={22} /> : <IconStarOutline size={22} />}
                    </button>
                  </div>
                  <div className={styles.metaRow}>
                    <span className={styles.readtime}>
                      <IconClock size={15} /> ~{minutes} min de lecture
                    </span>
                    {completed && <Badge tone="success">Terminée</Badge>}
                  </div>
                </header>

                <article className={styles.content} ref={articleRef}>
                  {sections.map((section) => (
                    <SectionView key={section.id} section={section} />
                  ))}
                </article>

                <div className={styles.completeBar}>
                  <Button onClick={() => void complete()} disabled={saving || completed}>
                    {completed ? (
                      <>
                        <IconCheck size={16} /> Leçon terminée
                      </>
                    ) : saving ? (
                      'Enregistrement…'
                    ) : (
                      'Marquer comme terminée'
                    )}
                  </Button>
                  {completed && next && (
                    <Button variant="secondary" onClick={() => navigate(`/lessons/${next.id}`)}>
                      Leçon suivante →
                    </Button>
                  )}
                </div>

                <nav className={styles.pager}>
                  <Button
                    variant="ghost"
                    disabled={!prev}
                    onClick={() => prev && navigate(`/lessons/${prev.id}`)}
                  >
                    ← Précédent
                  </Button>
                  <Button variant="ghost" onClick={() => navigate(`/nodes/${node.id}`)}>
                    Retour au nœud
                  </Button>
                  <Button
                    variant="ghost"
                    disabled={!next}
                    onClick={() => next && navigate(`/lessons/${next.id}`)}
                  >
                    Suivant →
                  </Button>
                </nav>

                <LessonNotes lessonId={id} />
              </div>

              {/* --- Sommaire "sur cette page" --- */}
              {tocItems.length > 1 && (
                <aside className={styles.toc} aria-label="Sur cette page">
                  <div className={styles.tocTitle}>Sur cette page</div>
                  <ul className={styles.tocList}>
                    {tocItems.map((item) => (
                      <li key={item.id}>
                        <button
                          type="button"
                          className={[styles.tocLink, activeSection === item.id ? styles.tocActive : '']
                            .filter(Boolean)
                            .join(' ')}
                          onClick={() => goToSection(item.id)}
                        >
                          {deEmDash(item.title)}
                        </button>
                      </li>
                    ))}
                  </ul>
                </aside>
              )}
            </div>
          )
        }}
      </AsyncSection>
    </div>
  )
}

/** Emojis du contenu → icônes SVG (aucun emoji ne doit s'afficher dans le front). */
function emojiIcon(token: string, key: number): ReactNode | null {
  switch (token) {
    case '👉':
      return <IconInsight key={key} className={styles.iconInsight} title="À noter" />
    case '⚠️':
    case '⚠':
      return <IconWarning key={key} className={styles.iconWarn} title="Attention" />
    case '✅':
    case '✔️':
    case '✓':
      return <IconCheck key={key} className={styles.iconOk} title="Correct" />
    case '❌':
    case '✗':
    case '✕':
      return <IconCross key={key} className={styles.iconKo} title="Incorrect" />
    default:
      return null
  }
}

function formatInline(raw: string): ReactNode[] {
  const text = deEmDash(raw)
  const nodes: ReactNode[] = []
  const regex = /(\*\*[^*]+\*\*|\*[^*\n]+\*|`[^`]+`|👉|⚠️|⚠|✅|✔️|✓|❌|✗|✕)/gu
  let last = 0
  let key = 0
  let match: RegExpExecArray | null
  while ((match = regex.exec(text)) !== null) {
    if (match.index > last) nodes.push(text.slice(last, match.index))
    const token = match[0]
    const icon = emojiIcon(token, key)
    if (icon) {
      nodes.push(icon)
      key++
    } else if (token.startsWith('**')) {
      nodes.push(<strong key={key++}>{token.slice(2, -2)}</strong>)
    } else if (token.startsWith('`')) {
      nodes.push(
        <code key={key++} className={styles.inlineCode}>
          {token.slice(1, -1)}
        </code>,
      )
    } else {
      nodes.push(<em key={key++}>{token.slice(1, -1)}</em>)
    }
    last = regex.lastIndex
  }
  if (last < text.length) nodes.push(text.slice(last))
  return nodes
}

const UL_LINE = /^\s*[-*]\s+(.*)$/
const OL_LINE = /^\s*\d+[.)]\s+(.*)$/

function renderBlock(block: string, keyBase: string): ReactNode[] {
  const lines = block.split('\n')
  const out: ReactNode[] = []
  let i = 0
  let k = 0
  while (i < lines.length) {
    if (UL_LINE.test(lines[i])) {
      const items: string[] = []
      while (i < lines.length && UL_LINE.test(lines[i])) {
        items.push(lines[i].replace(UL_LINE, '$1'))
        i++
      }
      out.push(
        <ul key={`${keyBase}-${k++}`} className={styles.list}>
          {items.map((it, j) => (
            <li key={j}>{formatInline(it)}</li>
          ))}
        </ul>,
      )
    } else if (OL_LINE.test(lines[i])) {
      const items: string[] = []
      let j = i
      while (j < lines.length && OL_LINE.test(lines[j])) {
        items.push(lines[j].replace(OL_LINE, '$1'))
        j++
      }
      if (items.length >= 2) {
        out.push(
          <ol key={`${keyBase}-${k++}`} className={styles.list}>
            {items.map((it, m) => (
              <li key={m}>{formatInline(it)}</li>
            ))}
          </ol>,
        )
        i = j
      } else {
        out.push(
          <p key={`${keyBase}-${k++}`} className={styles.paragraph}>
            {formatInline(lines[i].trim())}
          </p>,
        )
        i++
      }
    } else {
      const buff: string[] = []
      while (i < lines.length && !UL_LINE.test(lines[i]) && !OL_LINE.test(lines[i])) {
        if (lines[i].trim()) buff.push(lines[i].trim())
        i++
      }
      if (buff.length) {
        out.push(
          <p key={`${keyBase}-${k++}`} className={styles.paragraph}>
            {formatInline(buff.join(' '))}
          </p>,
        )
      }
    }
  }
  return out
}

/** Rend un corps de texte en blocs (séparés par lignes vides) : paragraphes, listes, formatage inline. */
function Prose({ text }: { text: string }) {
  const blocks = text
    .split(/\n{2,}/)
    .map((b) => b.replace(/\s+$/g, ''))
    .filter((b) => b.trim())
  return <>{blocks.flatMap((block, i) => renderBlock(block, `b${i}`))}</>
}

/** Rendu d'une section typée de leçon, dans une carte dont la variante dépend du type. */
function SectionView({ section }: { section: Section }) {
  const body = section.body ?? ''
  const meta = sectionKind(section.type)
  const anchor = `sec-${section.id}`

  const head = section.title ? (
    <div className={styles.sectionHead}>
      {meta.kind !== 'concept' && (
        <span className={styles.kindChip} data-kind={meta.kind}>
          {meta.label}
        </span>
      )}
      <h2 className={styles.sectionTitle}>{deEmDash(section.title)}</h2>
    </div>
  ) : null

  let inner: ReactNode
  switch (section.type) {
    case 'CODE':
      inner = <CodeBlock code={body} language={section.codeLanguage ?? 'java'} />
      break
    case 'CALLOUT':
      inner = (
        <Callout tone="info" title={section.title ?? undefined}>
          <Prose text={body} />
        </Callout>
      )
      break
    case 'DIAGRAM':
      inner =
        (section.diagramFormat ?? '').toUpperCase() === 'MERMAID' ? (
          <MermaidDiagram code={body} />
        ) : (
          <figure className={styles.diagram}>
            <pre className={styles.diagramBody}>{body}</pre>
            {section.diagramFormat && (
              <figcaption className={styles.diagramCaption}>
                Diagramme ({section.diagramFormat})
              </figcaption>
            )}
          </figure>
        )
      break
    case 'QUIZ':
      inner = section.quizId ? (
        <QuizRunner quizId={section.quizId} />
      ) : (
        <p className={styles.paragraph}>Quiz indisponible.</p>
      )
      break
    case 'EXERCISE':
      inner = (
        <>
          <Prose text={body} />
          {section.solution && (
            <details className={styles.solution}>
              <summary className={styles.solutionSummary}>Voir la solution</summary>
              <div className={styles.solutionBody}>
                <Prose text={section.solution} />
              </div>
            </details>
          )}
        </>
      )
      break
    case 'IMAGE':
      // body = chemin de l'image ; title = légende (facultative).
      inner = (
        <figure className={styles.figure}>
          <img src={body} alt={section.title ?? ''} loading="lazy" className={styles.figureImg} />
          {section.title && (
            <figcaption className={styles.figureCaption}>{deEmDash(section.title)}</figcaption>
          )}
        </figure>
      )
      break
    default:
      inner = <Prose text={body} />
  }

  return (
    <section id={anchor} className={styles.section} data-kind={meta.kind}>
      {section.type === 'CALLOUT' || section.type === 'IMAGE' ? inner : (
        <>
          {head}
          {inner}
        </>
      )}
    </section>
  )
}
