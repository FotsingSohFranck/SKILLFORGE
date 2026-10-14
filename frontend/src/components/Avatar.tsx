interface AvatarProps {
  name: string
  size?: number
}

/** Avatar simple : cercle dégradé avec l'initiale de l'utilisateur. */
export function Avatar({ name, size = 32 }: AvatarProps) {
  const initial = (name?.trim()?.[0] ?? '?').toUpperCase()
  return (
    <span
      aria-hidden="true"
      style={{
        flex: 'none',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: size,
        height: size,
        borderRadius: '50%',
        background: 'var(--color-primary)',
        color: 'var(--color-on-primary)',
        fontWeight: 700,
        fontSize: size * 0.45,
      }}
    >
      {initial}
    </span>
  )
}
