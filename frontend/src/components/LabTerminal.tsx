import { useEffect, useRef, useState } from 'react'
import { Terminal } from '@xterm/xterm'
import { FitAddon } from '@xterm/addon-fit'
import '@xterm/xterm/css/xterm.css'
import { requestTerminalTicket, terminalSocketUrl } from '../api/lab'
import { ApiError } from '../api/types'
import { Button } from './Button'
import { useToasts } from './toast/toastStore'
import styles from './LabTerminal.module.css'

interface Props {
  nodeId: string
}

type Status = 'connecting' | 'connected' | 'closed'

const STATUS_LABEL: Record<Status, string> = {
  connecting: 'Connexion…',
  connected: 'Connecté',
  closed: 'Fermé',
}

/** Terminal interactif (xterm.js) relié au shell d'un conteneur de lab via WebSocket (SF-91f). */
export function LabTerminal({ nodeId }: Props) {
  const push = useToasts((s) => s.push)
  const [open, setOpen] = useState(false)
  const [status, setStatus] = useState<Status>('connecting')
  const hostRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open || !hostRef.current) return
    let disposed = false
    setStatus('connecting')

    const term = new Terminal({
      fontSize: 13,
      fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
      cursorBlink: true,
      theme: { background: '#0f1117', foreground: '#e6e6e6', cursor: '#c4b5fd' },
    })
    const fit = new FitAddon()
    term.loadAddon(fit)
    term.open(hostRef.current)
    safeFit(fit)

    const encoder = new TextEncoder()
    let socket: WebSocket | null = null

    void (async () => {
      try {
        const { ticket } = await requestTerminalTicket(nodeId, term.cols, term.rows)
        if (disposed) return
        const ws = new WebSocket(terminalSocketUrl(ticket))
        ws.binaryType = 'arraybuffer'
        socket = ws

        ws.onopen = () => {
          if (disposed) return
          setStatus('connected')
          term.focus()
        }
        ws.onmessage = (ev) => {
          if (typeof ev.data === 'string') term.write(ev.data)
          else term.write(new Uint8Array(ev.data as ArrayBuffer))
        }
        ws.onclose = (ev) => {
          if (disposed) return
          setStatus('closed')
          if (ev.reason) term.write(`\r\n\x1b[31m${ev.reason}\x1b[0m\r\n`)
        }
        ws.onerror = () => {
          if (!disposed) setStatus('closed')
        }

        term.onData((data) => {
          if (ws.readyState === WebSocket.OPEN) ws.send(encoder.encode(data))
        })
        term.onResize(({ cols, rows }) => {
          if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify({ resize: { cols, rows } }))
        })
      } catch (err) {
        if (disposed) return
        setStatus('closed')
        push('danger', err instanceof ApiError ? err.message : "Terminal indisponible : l'environnement de pratique n'est pas activé.")
      }
    })()

    const onWindowResize = () => safeFit(fit)
    window.addEventListener('resize', onWindowResize)

    return () => {
      disposed = true
      window.removeEventListener('resize', onWindowResize)
      try {
        socket?.close()
      } catch {
        // socket déjà fermé
      }
      term.dispose()
    }
  }, [open, nodeId, push])

  if (!open) {
    return (
      <div className={styles.launch}>
        <Button variant="ghost" onClick={() => setOpen(true)}>
          Ouvrir un terminal
        </Button>
        <span className={styles.hint}>Un environnement isolé, sans accès réseau, limité dans le temps.</span>
      </div>
    )
  }

  return (
    <div className={styles.wrap}>
      <div className={styles.head}>
        <span className={styles.status} data-state={status}>
          {STATUS_LABEL[status]}
        </span>
        <button type="button" className={styles.close} onClick={() => setOpen(false)}>
          Fermer le terminal
        </button>
      </div>
      <div ref={hostRef} className={styles.term} />
    </div>
  )
}

function safeFit(fit: FitAddon) {
  try {
    fit.fit()
  } catch {
    // conteneur pas encore mesurable : la taille par défaut reste utilisable
  }
}
