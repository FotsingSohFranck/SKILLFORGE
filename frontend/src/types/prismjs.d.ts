// Déclarations minimales pour Prism.js (pas de @types installés).
declare module 'prismjs' {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  type Grammar = any
  const Prism: {
    languages: Record<string, Grammar>
    highlight(text: string, grammar: Grammar, language: string): string
  }
  export default Prism
}

// Les grammaires sont importées pour leur effet de bord (enregistrement sur Prism).
declare module 'prismjs/components/*'
