/**
 * Déclenche une célébration visuelle (confettis) écoutée par le composant <Confetti />.
 * Découplé via un évènement window pour pouvoir être appelé aussi hors React (stores).
 */
export function celebrate(): void {
  try {
    window.dispatchEvent(new CustomEvent('sf-celebrate'))
  } catch {
    // environnement sans window : sans conséquence
  }
}
