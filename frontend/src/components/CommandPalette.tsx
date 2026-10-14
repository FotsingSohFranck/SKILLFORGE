import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getRoadmap, listRoadmaps } from '../api/catalogue'
import { deEmDash } from '../format'
import styles from './CommandPalette.module.css'

interface Item {
  kind: 'Parcours' | 'Nœud'
  label: string
  sub?: string
  to: string
}

/** Palette de recherche globale (Cmd/Ctrl+K) : accès rapide aux parcours et aux nœuds. */
export function CommandPalette() {
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [items, setItems] = useState<Item[] | null>(null)
  const [query, setQuery] = useState('')
  const [active, setActive] = useState(0)
  const inputRef = useRef<HTMLInputElement>(null)

  // Raccourci global d'ouverture/fermeture.
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault()
        setOpen((v) => !v)
      } else if (e.key === 'Escape') {
        setOpen(false)
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  // Charge l'index (parcours + nœuds) à la première ouverture.
  useEffect(() => {
    if (!open || items) return
    let cancelled = false
    ;(async () => {
      try {
        const roadmaps = await listRoadmaps()
        const graphs = await Promise.all(roadmaps.map((r) => getRoadmap(r.slug).catch(() => null)))
        if (cancelled) return
        const list: Item[] = []
        graphs.forEach((g) => {
          if (!g) return
          list.push({ kind: 'Parcours', label: g.roadmap.title, to: `/roadmaps/${g.roadmap.slug}` })
          g.nodes.forEach((n) =>
            list.push({ kind: 'Nœud', label: n.title, sub: g.roadmap.title, to: `/nodes/${n.id}` }),
          )
        })
        setItems(list)
      } catch {
        setItems([])
      }
    })()
    return () => {
      cancelled = true
    }
  }, [open, items])

  useEffect(() => {
    if (open) {
      setQuery('')
      setActive(0)
      setTimeout(() => inputRef.current?.focus(), 0)
    }
  }, [open])

  const results = useMemo(() => {
    const base = items ?? []
    const q = query.trim().toLowerCase()
    if (!q) return base.slice(0, 12)
    const scored = base
      .map((it) => {
        const l = it.label.toLowerCase()
        const idx = l.indexOf(q)
        if (idx === -1) return null
        return { it, score: (idx === 0 ? 0 : 1) + idx / 100 }
      })
      .filter((x): x is { it: Item; score: number } => x !== null)
      .sort((a, b) => a.score - b.score)
      .slice(0, 20)
    return scored.map((s) => s.it)
  }, [items, query])

  useEffect(() => setActive(0), [query])

  function go(item: Item) {
    setOpen(false)
    navigate(item.to)
  }

  function onInputKey(e: React.KeyboardEvent) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      setActive((a) => Math.min(a + 1, results.length - 1))
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      setActive((a) => Math.max(a - 1, 0))
    } else if (e.key === 'Enter' && results[active]) {
      e.preventDefault()
      go(results[active])
    }
  }

  if (!open) return null

  return (
    <div className={styles.overlay} onClick={() => setOpen(false)} role="presentation">
      <div
        className={styles.panel}
        role="dialog"
        aria-modal="true"
        aria-label="Recherche"
        onClick={(e) => e.stopPropagation()}
      >
        <input
          ref={inputRef}
          className={styles.input}
          placeholder="Rechercher un parcours ou un nœud…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={onInputKey}
        />
        <ul className={styles.list}>
          {items === null && <li className={styles.hint}>Chargement…</li>}
          {items !== null && results.length === 0 && (
            <li className={styles.hint}>Aucun résultat</li>
          )}
          {results.map((it, i) => (
            <li key={`${it.to}-${i}`}>
              <button
                type="button"
                className={[styles.item, 'sf-pressable', i === active ? styles.active : ''].filter(Boolean).join(' ')}
                onMouseEnter={() => setActive(i)}
                onClick={() => go(it)}
              >
                <span className={styles.kind}>{it.kind}</span>
                <span className={styles.label}>{deEmDash(it.label)}</span>
                {it.sub && <span className={styles.sub}>{deEmDash(it.sub)}</span>}
              </button>
            </li>
          ))}
        </ul>
        <div className={styles.footer}>
          <span>↑↓ naviguer</span>
          <span>↵ ouvrir</span>
          <span>Échap fermer</span>
        </div>
      </div>
    </div>
  )
}
