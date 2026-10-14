import { Button } from './Button'
import { IconMoon, IconSun } from './icons'
import { useTheme } from '../theme/themeStore'

/** Bascule clair/sombre du thème global. */
export function ThemeToggle() {
  const theme = useTheme((s) => s.theme)
  const toggle = useTheme((s) => s.toggle)
  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={toggle}
      aria-label={theme === 'dark' ? 'Passer au thème clair' : 'Passer au thème sombre'}
    >
      {theme === 'dark' ? (
        <>
          <IconSun size={16} /> Clair
        </>
      ) : (
        <>
          <IconMoon size={16} /> Sombre
        </>
      )}
    </Button>
  )
}
