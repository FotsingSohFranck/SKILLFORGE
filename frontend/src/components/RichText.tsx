import type { ReactNode } from 'react'
import { CodeBlock } from './CodeBlock'
import styles from './RichText.module.css'

/**
 * Rendu Markdown léger : paragraphes, listes, gras/italique, code inline, et blocs de code
 * clôturés (``` ```) avec coloration syntaxique via {@link CodeBlock}. Utilisé notamment pour
 * les consignes d'épreuve pratique afin qu'elles soient rendues comme le contenu des leçons.
 */

const FENCE = /```([a-zA-Z0-9+#-]*)\n([\s\S]*?)```/g
const UL_LINE = /^\s*[-*]\s+(.*)$/
const OL_LINE = /^\s*\d+[.)]\s+(.*)$/

function formatInline(raw: string): ReactNode[] {
  const nodes: ReactNode[] = []
  const regex = /(\*\*[^*]+\*\*|\*[^*\n]+\*|`[^`]+`)/g
  let last = 0
  let key = 0
  let match: RegExpExecArray | null
  while ((match = regex.exec(raw)) !== null) {
    if (match.index > last) nodes.push(raw.slice(last, match.index))
    const token = match[0]
    if (token.startsWith('**')) {
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
  if (last < raw.length) nodes.push(raw.slice(last))
  return nodes
}

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
      while (i < lines.length && OL_LINE.test(lines[i])) {
        items.push(lines[i].replace(OL_LINE, '$1'))
        i++
      }
      out.push(
        <ol key={`${keyBase}-${k++}`} className={styles.list}>
          {items.map((it, j) => (
            <li key={j}>{formatInline(it)}</li>
          ))}
        </ol>,
      )
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

function renderProse(text: string, keyBase: string): ReactNode[] {
  return text
    .split(/\n{2,}/)
    .map((b) => b.replace(/\s+$/g, ''))
    .filter((b) => b.trim())
    .flatMap((block, i) => renderBlock(block, `${keyBase}-${i}`))
}

export function RichText({ text }: { text: string }) {
  const parts: ReactNode[] = []
  let last = 0
  let key = 0
  let m: RegExpExecArray | null
  FENCE.lastIndex = 0
  while ((m = FENCE.exec(text)) !== null) {
    if (m.index > last) {
      parts.push(<div key={key++}>{renderProse(text.slice(last, m.index), `p${key}`)}</div>)
    }
    parts.push(<CodeBlock key={key++} code={m[2].replace(/\n$/, '')} language={m[1] || 'java'} />)
    last = FENCE.lastIndex
  }
  if (last < text.length) {
    parts.push(<div key={key++}>{renderProse(text.slice(last), `p${key}`)}</div>)
  }
  return <>{parts}</>
}
