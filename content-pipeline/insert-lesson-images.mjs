// Insère UNE image APRES chaque concept d'un nœud (concept -> image -> concept -> image...),
// pour que l'apprenant lise la notion puis voie l'illustration. Autant d'images que de concepts.
//
// Usage :
//   node insert-lesson-images.mjs <node.json> <captions.json>
//   - node.json     : content/<parcours>/nodes/<node>.json
//   - captions.json : { "<lessonSlug>": [{ "cap": "légende concept 1" }, ...] }  (ordre des concepts)
//
// Les images attendues : frontend/public/images/lessons/<lessonSlug>-<n>.jpg (n = 1..nb concepts).
// Idempotent : retire d'abord les IMAGE existantes, puis les re-place.

import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import path from 'node:path'

const [, , nodeFile, capFile] = process.argv
if (!nodeFile || !capFile) {
  console.error('Usage: node insert-lesson-images.mjs <node.json> <captions.json>')
  process.exit(1)
}

const IMG_DIR = path.resolve('../frontend/public/images/lessons')
const data = JSON.parse(readFileSync(nodeFile, 'utf8'))
const caps = JSON.parse(readFileSync(capFile, 'utf8'))

let changed = 0
let missing = 0
for (const lesson of data.lessons) {
  const lcaps = caps[lesson.slug]
  if (!lcaps) continue

  const base = lesson.sections.filter((s) => s.type !== 'IMAGE').sort((a, b) => a.position - b.position)
  const result = []
  let c = 0
  for (const s of base) {
    result.push(s)
    if (s.type === 'CONCEPT' && c < lcaps.length) {
      const n = c + 1
      const file = path.join(IMG_DIR, `${lesson.slug}-${n}.jpg`)
      if (!existsSync(file)) {
        console.log(`  ! image manquante : ${lesson.slug}-${n}.jpg (concept sauté)`)
        missing++
      } else {
        result.push({
          type: 'IMAGE',
          title: lcaps[c].cap,
          body: `/images/lessons/${lesson.slug}-${n}.jpg`,
        })
      }
      c++
    }
  }
  result.forEach((s, i) => (s.position = i))
  lesson.sections = result
  changed++
  const imgs = result.filter((s) => s.type === 'IMAGE').length
  console.log(`✓ ${lesson.slug} : ${imgs} image(s) après concepts`)
}

writeFileSync(nodeFile, JSON.stringify(data, null, 2) + '\n', 'utf8')
console.log(`\n${changed} leçon(s) traitée(s)${missing ? `, ${missing} image(s) manquante(s)` : ''}.`)
