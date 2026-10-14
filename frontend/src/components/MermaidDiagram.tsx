import { useEffect, useId, useRef, useState } from 'react'
import { useTheme } from '../theme/themeStore'
import styles from './MermaidDiagram.module.css'

interface MermaidDiagramProps {
  code: string
  caption?: string
}

/**
 * Rend un diagramme Mermaid en SVG (mermaid chargé à la demande).
 * En cas d'échec de parsing/rendu, on retombe proprement sur la source.
 */
export function MermaidDiagram({ code, caption }: MermaidDiagramProps) {
  const theme = useTheme((s) => s.theme)
  const [svg, setSvg] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)
  const rawId = useId()
  const renderCount = useRef(0)

  useEffect(() => {
    let active = true
    setFailed(false)
    const id = `mmd-${rawId.replace(/[^a-zA-Z0-9]/g, '')}-${renderCount.current++}`
    void (async () => {
      try {
        const mermaid = (await import('mermaid')).default
        mermaid.initialize({
          startOnLoad: false,
          theme: theme === 'dark' ? 'dark' : 'default',
          securityLevel: 'strict',
        })
        const { svg: out } = await mermaid.render(id, code)
        if (active) setSvg(out)
      } catch {
        if (active) {
          setSvg(null)
          setFailed(true)
        }
      }
    })()
    return () => {
      active = false
    }
  }, [code, theme, rawId])

  return (
    <figure className={styles.diagram}>
      {svg && !failed ? (
        // SVG produit par mermaid en mode strict (assaini) — pas d'entrée utilisateur brute.
        <div className={styles.svg} dangerouslySetInnerHTML={{ __html: svg }} />
      ) : (
        <pre className={styles.source}>{code}</pre>
      )}
      {caption && <figcaption className={styles.caption}>{caption}</figcaption>}
    </figure>
  )
}
