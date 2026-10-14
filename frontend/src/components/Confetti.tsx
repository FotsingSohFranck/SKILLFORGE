import { useEffect, useRef } from 'react'

const COLORS = ['#1d1d20', '#53555c', '#a1a4ad', '#778eff', '#b68cff', '#e2e5ea']

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number
  color: string
  rot: number
  vr: number
  life: number
  ttl: number
}

/**
 * Confettis plein écran déclenchés par l'évènement « sf-celebrate ».
 * Rendu sur un canvas (aucune dépendance), respecte prefers-reduced-motion.
 */
export function Confetti() {
  const canvasRef = useRef<HTMLCanvasElement | null>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    const ctx = canvas?.getContext('2d')
    if (!canvas || !ctx) return
    const reduce = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false

    let raf = 0
    let running = false
    let particles: Particle[] = []

    function resize() {
      canvas!.width = window.innerWidth
      canvas!.height = window.innerHeight
    }
    resize()
    window.addEventListener('resize', resize)

    function spawn() {
      const cx = window.innerWidth / 2
      particles = Array.from({ length: 150 }, () => ({
        x: cx + (Math.random() - 0.5) * window.innerWidth * 0.5,
        y: -20 - Math.random() * window.innerHeight * 0.2,
        vx: (Math.random() - 0.5) * 7,
        vy: 2 + Math.random() * 5,
        size: 5 + Math.random() * 7,
        color: COLORS[Math.floor(Math.random() * COLORS.length)],
        rot: Math.random() * Math.PI,
        vr: (Math.random() - 0.5) * 0.3,
        life: 0,
        ttl: 90 + Math.random() * 50,
      }))
    }

    function tick() {
      ctx!.clearRect(0, 0, canvas!.width, canvas!.height)
      let alive = 0
      for (const p of particles) {
        p.life++
        if (p.life > p.ttl) continue
        alive++
        p.vy += 0.12
        p.x += p.vx
        p.y += p.vy
        p.rot += p.vr
        ctx!.save()
        ctx!.globalAlpha = Math.max(0, 1 - p.life / p.ttl)
        ctx!.translate(p.x, p.y)
        ctx!.rotate(p.rot)
        ctx!.fillStyle = p.color
        ctx!.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6)
        ctx!.restore()
      }
      if (alive > 0) {
        raf = requestAnimationFrame(tick)
      } else {
        running = false
        ctx!.clearRect(0, 0, canvas!.width, canvas!.height)
      }
    }

    function onCelebrate() {
      if (reduce) return
      spawn()
      if (!running) {
        running = true
        cancelAnimationFrame(raf)
        raf = requestAnimationFrame(tick)
      }
    }

    window.addEventListener('sf-celebrate', onCelebrate)
    return () => {
      window.removeEventListener('sf-celebrate', onCelebrate)
      window.removeEventListener('resize', resize)
      cancelAnimationFrame(raf)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      aria-hidden="true"
      style={{ position: 'fixed', inset: 0, pointerEvents: 'none', zIndex: 300 }}
    />
  )
}
