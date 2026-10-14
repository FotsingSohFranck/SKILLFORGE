// Validateur du contenu SkillForge (format pivot authoré).
// Usage : node validate.mjs [contentRoot] [--only <slug>]
//   contentRoot : dossier des parcours (défaut ../content). Chaque sous-dossier = un parcours.
// Sort en code 1 si au moins une erreur est trouvée.

import { basename, resolve } from 'node:path'
import { existsSync } from 'node:fs'
import { DEFAULT_CONTENT, listParcours, validateParcoursDir } from './lib/content.mjs'

function parseArgs(argv) {
  let contentRoot = DEFAULT_CONTENT
  let only = null
  for (let i = 0; i < argv.length; i++) {
    if (argv[i] === '--only') only = argv[++i]
    else contentRoot = resolve(argv[i])
  }
  return { contentRoot, only }
}

const rel = (p) => p.replace(process.cwd() + '\\', '').replace(process.cwd() + '/', '')

function main() {
  const { contentRoot, only } = parseArgs(process.argv.slice(2))
  if (!existsSync(contentRoot)) {
    console.error(`Dossier de contenu introuvable : ${contentRoot}`)
    process.exit(1)
  }
  const parcours = listParcours(contentRoot, only)
  if (parcours.length === 0) {
    console.log(`Aucun parcours à valider dans ${rel(contentRoot)}${only ? ` (filtre --only ${only})` : ''}.`)
    return
  }

  const errors = parcours.flatMap((dir) => validateParcoursDir(dir).map((e) => rel(e)))
  if (errors.length === 0) {
    console.log(`✓ ${parcours.length} parcours valide(s) : ${parcours.map((p) => basename(p)).join(', ')}`)
    return
  }
  console.error(`✗ ${errors.length} erreur(s) :\n`)
  for (const e of errors) console.error(`  - ${e}`)
  process.exit(1)
}

main()
