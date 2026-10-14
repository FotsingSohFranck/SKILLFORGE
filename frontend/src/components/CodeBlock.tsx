import { useMemo, useState } from 'react'
import Prism from 'prismjs'
// Les grammaires sont enregistrées en effet de bord sur l'objet Prism global.
// clike doit être chargé avant java (java en dépend).
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-java'
import 'prismjs/components/prism-sql'
import 'prismjs/components/prism-yaml'
import 'prismjs/components/prism-bash'
import 'prismjs/components/prism-rust'
import { IconCheck } from './icons'
import styles from './CodeBlock.module.css'

/** Les blocs de code ne doivent pas contenir d'emoji : on les remplace par des marqueurs texte. */
function sanitizeCode(code: string): string {
  return code
    .replace(/✅|✔️/g, 'OK :')
    .replace(/❌/g, 'NON :')
    .replace(/⚠️|⚠/g, 'ATTENTION :')
    .replace(/👉/g, '->')
    .replace(/[✓]/g, 'OK')
    .replace(/[✗✕]/g, 'X')
    .replace(/\s*[—–]\s*/g, ' - ')
}

function escapeHtml(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

interface CodeBlockProps {
  code: string
  language?: string
}

/**
 * Bloc de code avec coloration syntaxique via Prism.js (rendu HTML déterministe).
 * Les couleurs des jetons sont définies globalement (`.sf-prism .token.*`) et s'adaptent
 * au thème clair/sombre, avec un contraste visé WCAG AA.
 */
export function CodeBlock({ code, language = 'java' }: CodeBlockProps) {
  const [copied, setCopied] = useState(false)
  const cleanCode = sanitizeCode(code)

  const html = useMemo(() => {
    const grammar = Prism.languages[language] ?? Prism.languages.clike
    if (!grammar) return escapeHtml(cleanCode)
    try {
      return Prism.highlight(cleanCode, grammar, language)
    } catch {
      return escapeHtml(cleanCode)
    }
  }, [cleanCode, language])

  async function copy() {
    try {
      await navigator.clipboard.writeText(cleanCode)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // presse-papiers indisponible : on n'interrompt pas la lecture
    }
  }

  return (
    <div className={styles.wrap}>
      <div className={styles.head}>
        <span className={styles.lang}>{language}</span>
        <button
          type="button"
          className={styles.copy}
          onClick={() => void copy()}
          aria-label={copied ? 'Code copié' : 'Copier le code'}
        >
          {copied ? (
            <>
              <IconCheck size={14} /> Copié
            </>
          ) : (
            'Copier'
          )}
        </button>
      </div>
      <pre className={styles.pre}>
        <code
          className={`sf-prism language-${language}`}
          dangerouslySetInnerHTML={{ __html: html }}
        />
      </pre>
    </div>
  )
}
