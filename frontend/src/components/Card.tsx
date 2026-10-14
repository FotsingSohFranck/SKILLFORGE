import type { ElementType, HTMLAttributes, ReactNode } from 'react'
import styles from './Card.module.css'

interface CardProps extends Omit<HTMLAttributes<HTMLDivElement>, 'title'> {
  title?: ReactNode
  /** Élément de rendu du titre (`div` par défaut ; `h1`/`h2`… pour la hiérarchie). */
  titleAs?: ElementType
  /** Média pleine largeur en haut de carte (ex. couverture de parcours). */
  media?: ReactNode
  footer?: ReactNode
  children?: ReactNode
}

/** Conteneur de surface avec média, titre et pied optionnels. */
export function Card({ media, title, titleAs: TitleTag = 'div', footer, children, className, ...rest }: CardProps) {
  return (
    <div className={[styles.card, className].filter(Boolean).join(' ')} {...rest}>
      {media}
      {title && <TitleTag className={styles.header}>{title}</TitleTag>}
      <div className={styles.body}>{children}</div>
      {footer && <div className={styles.footer}>{footer}</div>}
    </div>
  )
}
