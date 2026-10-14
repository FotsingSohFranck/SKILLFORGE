import { IconCross } from '../icons'
import { useToasts } from './toastStore'
import styles from './ToastViewport.module.css'

/** Zone d'affichage des toasts (bas-droite). À monter une fois à la racine de l'app. */
export function ToastViewport() {
  const toasts = useToasts((s) => s.toasts)
  const dismiss = useToasts((s) => s.dismiss)
  return (
    <div className={styles.viewport} role="region" aria-label="Notifications" aria-live="polite">
      {toasts.map((toast) => (
        <div key={toast.id} className={[styles.toast, styles[toast.tone]].join(' ')}>
          <span>{toast.message}</span>
          <button className={styles.close} onClick={() => dismiss(toast.id)} aria-label="Fermer">
            <IconCross size={16} />
          </button>
        </div>
      ))}
    </div>
  )
}
