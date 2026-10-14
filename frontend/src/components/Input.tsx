import type { InputHTMLAttributes } from 'react'
import { useId, useState } from 'react'
import { IconEye, IconEyeOff } from './icons'
import styles from './Input.module.css'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
}

/** Champ de saisie avec label et message d'erreur accessibles.
 *  Pour les champs `type="password"`, un bouton œil permet d'afficher/masquer la saisie. */
export function Input({ label, error, id, className, type, ...rest }: InputProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const errorId = `${inputId}-error`
  const isPassword = type === 'password'
  const [reveal, setReveal] = useState(false)
  const effectiveType = isPassword && reveal ? 'text' : type

  const inputEl = (
    <input
      id={inputId}
      type={effectiveType}
      className={[styles.input, isPassword ? styles.withToggle : '', error ? styles.invalid : '']
        .filter(Boolean)
        .join(' ')}
      aria-invalid={error ? true : undefined}
      aria-describedby={error ? errorId : undefined}
      {...rest}
    />
  )

  return (
    <div className={[styles.field, className].filter(Boolean).join(' ')}>
      {label && (
        <label htmlFor={inputId} className={styles.label}>
          {label}
        </label>
      )}
      {isPassword ? (
        <div className={styles.control}>
          {inputEl}
          <button
            type="button"
            className={styles.toggle}
            onClick={() => setReveal((v) => !v)}
            aria-label={reveal ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
            aria-pressed={reveal}
            tabIndex={-1}
          >
            {reveal ? <IconEyeOff size={18} /> : <IconEye size={18} />}
          </button>
        </div>
      ) : (
        inputEl
      )}
      {error && (
        <span id={errorId} className={styles.error} role="alert">
          {error}
        </span>
      )}
    </div>
  )
}
