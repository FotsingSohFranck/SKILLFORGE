import { useState } from 'react'
import { Link } from 'react-router-dom'
import { changePassword, updateDisplayName } from '../api/auth'
import { listRoadmaps } from '../api/catalogue'
import { getStreak, listEnrollments } from '../api/learning'
import { getGamifiedProfile, listBadges } from '../api/gamification'
import {
  ApiError,
  type Badge,
  type Enrollment,
  type GamifiedProfile,
  type RoadmapSummary,
  type Streak,
} from '../api/types'
import { useAuth } from '../auth/authStore'
import { AsyncSection } from '../components/AsyncSection'
import { Avatar } from '../components/Avatar'
import { Badge as BadgeTag } from '../components/Badge'
import { Button } from '../components/Button'
import { Card } from '../components/Card'
import { IconBook, IconFlame, IconMedal } from '../components/icons'
import { Input } from '../components/Input'
import { Progress } from '../components/Progress'
import { useToasts } from '../components/toast/toastStore'
import { useAsync } from '../hooks/useAsync'
import styles from './ProfilePage.module.css'

interface ProfileData {
  profile: GamifiedProfile
  badges: Badge[]
  streak: Streak
  enrollments: Enrollment[]
  roadmaps: RoadmapSummary[]
}

const ROLE_LABEL: Record<string, string> = {
  LEARNER: 'Apprenant',
  INSTRUCTOR: 'Formateur',
  ADMIN: 'Administrateur',
}

/** Paliers graphite monochromes pour les badges : sobre, sans arc-en-ciel. */
const BADGE_COLORS = ['#2c2c31', '#3a3b40', '#53555c', '#6b6d75', '#8a8d96', '#a1a4ad']

async function loadProfile(): Promise<ProfileData> {
  const [profile, badges, streak, enrollments, roadmaps] = await Promise.all([
    getGamifiedProfile(),
    listBadges(),
    getStreak(),
    listEnrollments().catch(() => []),
    listRoadmaps().catch(() => []),
  ])
  return { profile, badges, streak, enrollments, roadmaps }
}

function levelPercent(profile: GamifiedProfile): number {
  const { currentLevel, nextLevel, totalXp } = profile
  if (!currentLevel || !nextLevel) return 100
  const span = nextLevel.minXp - currentLevel.minXp
  if (span <= 0) return 100
  return ((totalXp - currentLevel.minXp) / span) * 100
}

function formatDate(iso: string | null): string {
  if (!iso) return ''
  const date = new Date(iso)
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString('fr-FR')
}

export function ProfilePage() {
  const user = useAuth((s) => s.user)
  const state = useAsync(loadProfile, [])

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <Avatar name={user?.displayName ?? '?'} size={64} />
        <div>
          <h1 className={styles.name}>{user?.displayName}</h1>
          <p className={styles.email}>{user?.email}</p>
          <div className={styles.roles}>
            {user?.roles.map((role) => (
              <BadgeTag key={role} tone={role === 'ADMIN' ? 'primary' : 'neutral'}>
                {ROLE_LABEL[role] ?? role}
              </BadgeTag>
            ))}
          </div>
        </div>
      </header>

      <AsyncSection state={state}>
        {({ profile, badges, streak, enrollments, roadmaps }) => {
          const roadmapById = new Map(roadmaps.map((r) => [r.id, r]))
          const inProgress = enrollments.filter((e) => e.progressPercent > 0 && e.progressPercent < 100).length
          const doneTracks = enrollments.filter(
            (e) => e.progressPercent >= 100 && roadmapById.get(e.roadmapId)?.kind !== 'CERTIFICATION',
          )

          return (
            <>
              {/* Tuiles de statistiques */}
              <div className={`${styles.stats} sf-stagger`}>
                <div className={styles.tile}>
                  <span className={styles.tileIcon} data-tone="medal" aria-hidden="true">
                    <IconMedal size={20} />
                  </span>
                  <div>
                    <div className={styles.tileValue}>{profile.currentLevel?.name ?? 'Débutant'}</div>
                    <div className={styles.tileLabel}>{profile.totalXp} XP</div>
                  </div>
                </div>
                <div className={styles.tile}>
                  <span className={styles.tileIcon} data-tone="flame" aria-hidden="true">
                    <IconFlame size={20} />
                  </span>
                  <div>
                    <div className={styles.tileValue}>
                      {streak.currentLength} jour{streak.currentLength > 1 ? 's' : ''}
                    </div>
                    <div className={styles.tileLabel}>Série (record : {streak.longestLength})</div>
                  </div>
                </div>
                <div className={styles.tile}>
                  <span className={styles.tileIcon} data-tone="book" aria-hidden="true">
                    <IconBook size={20} />
                  </span>
                  <div>
                    <div className={styles.tileValue}>{inProgress}</div>
                    <div className={styles.tileLabel}>Parcours en cours</div>
                  </div>
                </div>
                <div className={styles.tile}>
                  <span className={styles.tileIcon} data-tone="check" aria-hidden="true">
                    <IconMedal size={20} />
                  </span>
                  <div>
                    <div className={styles.tileValue}>{doneTracks.length}</div>
                    <div className={styles.tileLabel}>Parcours terminés</div>
                  </div>
                </div>
              </div>

              <Card title="Niveau & XP">
                <div className={styles.levelRow}>
                  <span className={styles.level}>{profile.currentLevel?.name ?? 'Débutant'}</span>
                  <span className={styles.xp}>{profile.totalXp} XP</span>
                </div>
                <Progress
                  value={levelPercent(profile)}
                  label={
                    profile.nextLevel
                      ? `${profile.xpToNextLevel} XP → ${profile.nextLevel.name}`
                      : 'Niveau maximum atteint'
                  }
                />
              </Card>

              {/* Certificats */}
              {doneTracks.length > 0 && (
                <section className={styles.section}>
                  <h2 className={styles.sectionTitle}>Mes certificats</h2>
                  <div className={styles.certs}>
                    {doneTracks.map((e) => {
                      const r = roadmapById.get(e.roadmapId)
                      if (!r) return null
                      return (
                        <Link key={e.id} to={`/certificate/${r.slug}`} className={`${styles.cert} sf-lift sf-pressable`}>
                          <span className={styles.certIcon} aria-hidden="true">
                            <IconMedal size={20} />
                          </span>
                          <span className={styles.certTitle}>{r.title}</span>
                          <span className={styles.certCta}>Voir →</span>
                        </Link>
                      )
                    })}
                  </div>
                </section>
              )}

              <section className={styles.section}>
                <h2 className={styles.sectionTitle}>Badges ({badges.length})</h2>
                {badges.length === 0 ? (
                  <p className={styles.empty}>
                    Aucun badge pour l'instant : termine des leçons et des nœuds pour en gagner.
                  </p>
                ) : (
                  <div className={`${styles.badges} sf-stagger`}>
                    {badges.map((badge, i) => (
                      <div key={badge.code} className={styles.badgeCard}>
                        <span
                          className={styles.badgeIcon}
                          style={{ background: BADGE_COLORS[i % BADGE_COLORS.length] }}
                          aria-hidden="true"
                        >
                          <IconMedal size={22} />
                        </span>
                        <div className={styles.badgeName}>{badge.name}</div>
                        {badge.description && <div className={styles.badgeDesc}>{badge.description}</div>}
                        {badge.earnedAt && (
                          <div className={styles.badgeDate}>Obtenu le {formatDate(badge.earnedAt)}</div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </section>

              <AccountCard />

              <details className={styles.howtoWrap}>
                <summary className={styles.howtoSummary}>Comment ça marche</summary>
                <ul className={styles.howto}>
                  <li>
                    Termine des <strong>leçons</strong> et des <strong>nœuds</strong> pour gagner de l'
                    <strong>XP</strong>.
                  </li>
                  <li>
                    L'XP accumulée te fait <strong>monter de niveau</strong>.
                  </li>
                  <li>
                    Débloque des <strong>badges</strong> en franchissant des étapes clés.
                  </li>
                  <li>
                    Apprends <strong>chaque jour</strong> pour entretenir ta <strong>série</strong>.
                  </li>
                </ul>
              </details>
            </>
          )
        }}
      </AsyncSection>
    </div>
  )
}

/** Carte « Mon compte » : modifier son nom + changer son mot de passe. */
function AccountCard() {
  const user = useAuth((s) => s.user)
  const reloadUser = useAuth((s) => s.loadMe)
  const push = useToasts((s) => s.push)
  const [name, setName] = useState(user?.displayName ?? '')
  const [savingName, setSavingName] = useState(false)

  const [current, setCurrent] = useState('')
  const [next, setNext] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function saveName(e: React.FormEvent) {
    e.preventDefault()
    if (!name.trim()) return
    setSavingName(true)
    try {
      await updateDisplayName(name.trim())
      await reloadUser()
      push('success', 'Nom mis à jour')
    } catch (err) {
      push('danger', err instanceof ApiError ? err.message : 'Modification impossible.')
    } finally {
      setSavingName(false)
    }
  }

  async function savePassword(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    if (next.length < 8) {
      setError('Le nouveau mot de passe doit faire au moins 8 caractères.')
      return
    }
    if (next !== confirm) {
      setError('Les deux nouveaux mots de passe ne correspondent pas.')
      return
    }
    setLoading(true)
    try {
      await changePassword(current, next)
      push('success', 'Mot de passe mis à jour')
      setCurrent('')
      setNext('')
      setConfirm('')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Changement impossible.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card title="Mon compte">
      <form onSubmit={saveName} className={styles.accountForm}>
        <div className={styles.nameRow}>
          <Input label="Nom affiché" value={name} onChange={(e) => setName(e.target.value)} />
          <Button type="submit" variant="secondary" disabled={savingName || name.trim() === (user?.displayName ?? '')}>
            {savingName ? 'Enregistrement…' : 'Enregistrer'}
          </Button>
        </div>
      </form>

      <div className={styles.securityTitle}>Changer le mot de passe</div>
      <form onSubmit={savePassword} className={styles.accountForm}>
        {error && (
          <div className={styles.formError} role="alert">
            {error}
          </div>
        )}
        <Input
          label="Mot de passe actuel"
          type="password"
          autoComplete="current-password"
          required
          value={current}
          onChange={(e) => setCurrent(e.target.value)}
        />
        <Input
          label="Nouveau mot de passe"
          type="password"
          autoComplete="new-password"
          required
          value={next}
          onChange={(e) => setNext(e.target.value)}
        />
        <Input
          label="Confirme le nouveau mot de passe"
          type="password"
          autoComplete="new-password"
          required
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
        />
        <Button type="submit" variant="primary" disabled={loading}>
          {loading ? 'Mise à jour…' : 'Changer le mot de passe'}
        </Button>
      </form>
    </Card>
  )
}
