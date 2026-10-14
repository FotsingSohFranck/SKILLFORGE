import { useEffect, useRef, useState } from 'react'
import styles from './LessonNotes.module.css'

const noteKey = (lessonId: string) => `lesson-note-${lessonId}`

function loadNote(lessonId: string): string {
  try {
    return localStorage.getItem(noteKey(lessonId)) ?? ''
  } catch {
    return ''
  }
}

function saveNote(lessonId: string, value: string): void {
  try {
    if (value.trim()) localStorage.setItem(noteKey(lessonId), value)
    else localStorage.removeItem(noteKey(lessonId))
  } catch {
    // stockage indisponible : sans conséquence
  }
}

/**
 * Bloc-notes personnel par leçon (stockage local, propre à cet appareil).
 * Une version synchronisée côté serveur reste un suivi possible.
 */
export function LessonNotes({ lessonId }: { lessonId: string }) {
  const [value, setValue] = useState('')
  const [saved, setSaved] = useState(false)
  const timer = useRef<number | null>(null)

  useEffect(() => {
    setValue(loadNote(lessonId))
    setSaved(false)
  }, [lessonId])

  function onChange(next: string) {
    setValue(next)
    setSaved(false)
    if (timer.current) window.clearTimeout(timer.current)
    timer.current = window.setTimeout(() => {
      saveNote(lessonId, next)
      setSaved(true)
    }, 500)
  }

  return (
    <details className={styles.wrap}>
      <summary className={styles.summary}>
        Mes notes
        {value.trim() && <span className={styles.badge}>•</span>}
      </summary>
      <div className={styles.body}>
        <textarea
          className={styles.textarea}
          placeholder="Écris tes notes sur cette leçon… (enregistrées sur cet appareil)"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          rows={6}
        />
        <div className={styles.status}>{saved ? 'Enregistré' : value ? 'Modification…' : ''}</div>
      </div>
    </details>
  )
}
