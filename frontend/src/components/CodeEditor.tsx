import { useMemo } from 'react'
import CodeMirror from '@uiw/react-codemirror'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'
import type { Extension } from '@codemirror/state'
import { useTheme } from '../theme/themeStore'
import styles from './CodeEditor.module.css'

interface CodeEditorProps {
  value: string
  onChange: (value: string) => void
  language?: string
  readOnly?: boolean
  minHeight?: string
}

/**
 * Éditeur de code interactif (CodeMirror) : coloration, indentation, numéros de ligne.
 * Hors-ligne (bundlé), adapté au thème clair/sombre. Pour l'instant la coloration Java
 * couvre aussi les langages proches ; les autres restent en texte avec numéros de ligne.
 */
export function CodeEditor({ value, onChange, language = 'java', readOnly, minHeight }: CodeEditorProps) {
  const theme = useTheme((s) => s.theme)

  const extensions = useMemo<Extension[]>(() => {
    // Java couvre une syntaxe de type C (utile aussi pour des extraits proches).
    if (['java', 'kotlin', 'groovy', 'rust'].includes(language)) return [java()]
    return []
  }, [language])

  return (
    <div className={styles.editor}>
      <CodeMirror
        value={value}
        onChange={onChange}
        theme={theme === 'dark' ? oneDark : 'light'}
        extensions={extensions}
        readOnly={readOnly}
        minHeight={minHeight ?? '320px'}
        basicSetup={{
          lineNumbers: true,
          highlightActiveLine: !readOnly,
          foldGutter: false,
          autocompletion: false,
        }}
      />
    </div>
  )
}
