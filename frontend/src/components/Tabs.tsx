import type { KeyboardEvent, ReactNode } from 'react'
import { useId, useRef, useState } from 'react'
import styles from './Tabs.module.css'

export interface TabItem {
  id: string
  label: string
  content: ReactNode
}

interface TabsProps {
  items: TabItem[]
  initialId?: string
}

/** Onglets accessibles (motif WAI-ARIA : tablist/tab/tabpanel, flèches, roving tabindex). */
export function Tabs({ items, initialId }: TabsProps) {
  const [active, setActive] = useState(initialId ?? items[0]?.id)
  const baseId = useId()
  const tabRefs = useRef<Record<string, HTMLButtonElement | null>>({})
  const current = items.find((t) => t.id === active) ?? items[0]

  function onKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    const index = items.findIndex((t) => t.id === active)
    let next = index
    if (event.key === 'ArrowRight') next = (index + 1) % items.length
    else if (event.key === 'ArrowLeft') next = (index - 1 + items.length) % items.length
    else if (event.key === 'Home') next = 0
    else if (event.key === 'End') next = items.length - 1
    else return
    event.preventDefault()
    const nextId = items[next].id
    setActive(nextId)
    tabRefs.current[nextId]?.focus()
  }

  return (
    <div className={styles.tabs}>
      <div className={styles.list} role="tablist" onKeyDown={onKeyDown}>
        {items.map((tab) => {
          const selected = tab.id === active
          return (
            <button
              key={tab.id}
              id={`${baseId}-tab-${tab.id}`}
              role="tab"
              aria-selected={selected}
              aria-controls={`${baseId}-panel-${tab.id}`}
              tabIndex={selected ? 0 : -1}
              ref={(el) => {
                tabRefs.current[tab.id] = el
              }}
              className={[styles.tab, selected ? styles.active : ''].filter(Boolean).join(' ')}
              onClick={() => setActive(tab.id)}
            >
              {tab.label}
            </button>
          )
        })}
      </div>
      <div
        className={styles.panel}
        role="tabpanel"
        id={`${baseId}-panel-${current?.id}`}
        aria-labelledby={`${baseId}-tab-${current?.id}`}
        tabIndex={0}
      >
        {current?.content}
      </div>
    </div>
  )
}
