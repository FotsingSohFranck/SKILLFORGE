// Déclarations de types pour les styles Prism importés en sous-chemin ESM.
declare module 'react-syntax-highlighter/dist/esm/styles/prism' {
  import type { CSSProperties } from 'react'
  export const oneDark: { [key: string]: CSSProperties }
  export const oneLight: { [key: string]: CSSProperties }
}

// Langages Prism importés individuellement (bundle réduit).
declare module 'react-syntax-highlighter/dist/esm/languages/prism/*' {
  const language: unknown
  export default language
}
