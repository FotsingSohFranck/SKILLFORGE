/**
 * Nettoyage typographique du contenu au rendu.
 * On ne veut pas de tiret cadratin « — » (ni demi-cadratin « – ») dans le texte affiché :
 * on les remplace par une virgule, qui se lit naturellement dans la plupart des phrases.
 */
export function deEmDash(text: string): string {
  if (!text) return text
  return text
    // « mot — mot » -> « mot, mot »
    .replace(/\s*[—–]\s*/g, ', ')
    // évite les virgules en double créées par un em-dash suivant déjà une virgule
    .replace(/,\s*,/g, ', ')
    // pas de virgule collée juste avant une ponctuation forte
    .replace(/,\s*([.;:!?…])/g, '$1')
    // pas de virgule en tout début de segment
    .replace(/^\s*,\s*/, '')
}
