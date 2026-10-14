import type { ReactNode } from 'react'
import { useState } from 'react'
import { Badge } from '../components/Badge'
import { Button } from '../components/Button'
import { Callout } from '../components/Callout'
import { Card } from '../components/Card'
import { CodeBlock } from '../components/CodeBlock'
import { Input } from '../components/Input'
import { Modal } from '../components/Modal'
import { NodeCard } from '../components/NodeCard'
import { Progress } from '../components/Progress'
import { Tabs } from '../components/Tabs'
import { ThemeToggle } from '../components/ThemeToggle'
import { ToastViewport } from '../components/toast/ToastViewport'
import { useToasts } from '../components/toast/toastStore'
import styles from './Gallery.module.css'

const SAMPLE_CODE = `var cache = new java.util.concurrent.ConcurrentHashMap<String, String>();
cache.computeIfAbsent("key", k -> expensiveLookup(k));`

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className={styles.section}>
      <h2 className={styles.sectionTitle}>{title}</h2>
      <div className={styles.row}>{children}</div>
    </section>
  )
}

/** Galerie « Storybook maison » : démontre tous les composants du design system en clair/sombre. */
export function Gallery() {
  const [modalOpen, setModalOpen] = useState(false)
  const push = useToasts((s) => s.push)

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1 className={styles.title}>SkillForge : Design System</h1>
          <p className={styles.subtitle}>Palette violet / bleu / blanc · thèmes clair & sombre · WCAG AA</p>
        </div>
        <ThemeToggle />
      </header>

      <main className={styles.main}>
        <Section title="Boutons">
          <Button variant="primary">Primaire</Button>
          <Button variant="secondary">Secondaire</Button>
          <Button variant="ghost">Fantôme</Button>
          <Button variant="danger">Danger</Button>
          <Button variant="primary" size="sm">Petit</Button>
          <Button variant="primary" disabled>Désactivé</Button>
        </Section>

        <Section title="Badges">
          <Badge tone="primary">Nouveau</Badge>
          <Badge tone="secondary">Info</Badge>
          <Badge tone="success">Terminé</Badge>
          <Badge tone="warning">En cours</Badge>
          <Badge tone="danger">Échec</Badge>
          <Badge tone="neutral">Non commencé</Badge>
        </Section>

        <Section title="Progression">
          <div className={styles.stack}>
            <Progress value={25} label="Backend Developer" />
            <Progress value={70} label="Caching" />
            <Progress value={100} label="Git" />
          </div>
        </Section>

        <Section title="Cartes de nœud (statuts)">
          <div className={styles.grid}>
            <NodeCard title="Internet" status="COMPLETED" percent={100} importance="CORE" />
            <NodeCard title="Caching" status="IN_PROGRESS" percent={50} importance="RECOMMENDED" />
            <NodeCard title="Kafka" status="NOT_STARTED" importance="OPTIONAL" />
          </div>
        </Section>

        <Section title="Onglets & carte">
          <Card title="Introduction au caching" style={{ maxWidth: 520, width: '100%' }}>
            <Tabs
              items={[
                { id: 'concept', label: 'Concept', content: <p style={{ margin: 0 }}>Un cache stocke des résultats coûteux à recalculer.</p> },
                { id: 'code', label: 'Code', content: <CodeBlock code={SAMPLE_CODE} language="java" /> },
                { id: 'quiz', label: 'Quiz', content: <p style={{ margin: 0 }}>Que signifie « TTL » ?</p> },
              ]}
            />
          </Card>
        </Section>

        <Section title="Encadrés">
          <div className={styles.stack}>
            <Callout tone="info" title="Astuce">Tu peux reprendre exactement où tu t'es arrêté.</Callout>
            <Callout tone="success" title="Bravo">Nœud terminé, +100 XP !</Callout>
            <Callout tone="warning" title="Prérequis">Ce nœud recommande de connaître SQL.</Callout>
            <Callout tone="danger" title="Temps écoulé">L'examen blanc est terminé.</Callout>
          </div>
        </Section>

        <Section title="Champs">
          <div style={{ maxWidth: 320, width: '100%' }}>
            <Input label="Adresse e-mail" placeholder="vous@exemple.dev" />
          </div>
          <div style={{ maxWidth: 320, width: '100%' }}>
            <Input label="Mot de passe" type="password" error="Au moins 8 caractères." defaultValue="123" />
          </div>
        </Section>

        <Section title="Overlays">
          <Button variant="primary" onClick={() => setModalOpen(true)}>Ouvrir une modale</Button>
          <Button variant="secondary" onClick={() => push('success', 'Leçon marquée comme terminée.')}>
            Afficher un toast
          </Button>
        </Section>
      </main>

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Reprendre l'apprentissage"
        footer={
          <>
            <Button variant="ghost" onClick={() => setModalOpen(false)}>Plus tard</Button>
            <Button variant="primary" onClick={() => setModalOpen(false)}>Reprendre</Button>
          </>
        }
      >
        <p style={{ margin: 0 }}>Tu étais sur le nœud « Caching », leçon « Introduction au caching ».</p>
      </Modal>

      <ToastViewport />
    </div>
  )
}
