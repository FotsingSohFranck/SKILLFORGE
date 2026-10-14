// Banque d'images SkillForge : télécharge des photos HAUTE QUALITÉ, gratuites et
// libres de droits (Pexels) vers frontend/public/images/tracks/<slug>.jpg.
//
// Pourquoi Pexels : licence commerciale libre SANS attribution obligatoire, et
// téléchargement + auto-hébergement EXPLICITEMENT autorisés (contrairement a Unsplash
// qui impose le hotlinking). On écrit tout de même un credits.json (bonne pratique).
//
// Prérequis : une clé API Pexels GRATUITE et instantanée (https://www.pexels.com/api/).
// Usage :
//   PEXELS_API_KEY=xxxxx node fetch-images.mjs            # télécharge les manquantes
//   PEXELS_API_KEY=xxxxx node fetch-images.mjs --force    # re-télécharge tout
//   PEXELS_API_KEY=xxxxx node fetch-images.mjs --only kubernetes,rust
//
// Idempotent : une image déja présente n'est PAS re-téléchargée (sauf --force).

import { mkdir, writeFile, readFile, access } from 'node:fs/promises'
import { constants } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const OUT_DIR = path.resolve(__dirname, '../frontend/public/images/tracks')
const CREDITS_FILE = path.join(OUT_DIR, 'credits.json')
const API = 'https://api.pexels.com/v1/search'

// Requête thématique par parcours : choisie pour des visuels pertinents et sobres.
const QUERIES = {
  'api-security': 'cyber security lock abstract',
  aws: 'cloud computing data center',
  backend: 'server room code programming',
  'backend-performance': 'network speed server performance',
  cka: 'kubernetes container orchestration',
  ckad: 'developer container kubernetes',
  demo: 'learning code laptop',
  docker: 'cargo shipping containers',
  'java-17-21': 'coffee code programming',
  kcna: 'cloud native computing',
  kubernetes: 'containers orchestration ship',
  'prompt-engineering': 'artificial intelligence neural abstract',
  rust: 'industrial metal gears',
  'software-architect': 'architecture blueprint building',
  'software-design': 'design workspace blueprint',
  terraform: 'terrain landscape aerial',
}

const args = process.argv.slice(2)
const FORCE = args.includes('--force')
const onlyArg = args[args.indexOf('--only') + 1]
const ONLY = args.includes('--only') && onlyArg ? onlyArg.split(',').map((s) => s.trim()) : null

const KEY = process.env.PEXELS_API_KEY
if (!KEY) {
  console.error(
    'Erreur : variable PEXELS_API_KEY manquante.\n' +
      'Obtiens une clé gratuite en 30s sur https://www.pexels.com/api/ puis relance :\n' +
      '  PEXELS_API_KEY=ta_cle node fetch-images.mjs',
  )
  process.exit(1)
}

async function exists(p) {
  try {
    await access(p, constants.F_OK)
    return true
  } catch {
    return false
  }
}

/** Cherche sur Pexels et renvoie la meilleure photo paysage haute résolution. */
async function searchBest(query) {
  const url = `${API}?query=${encodeURIComponent(query)}&orientation=landscape&size=large&per_page=5`
  const res = await fetch(url, { headers: { Authorization: KEY } })
  if (!res.ok) {
    throw new Error(`Pexels ${res.status} ${res.statusText} (${await res.text().catch(() => '')})`)
  }
  const data = await res.json()
  const photo = (data.photos ?? [])[0]
  if (!photo) return null
  return {
    id: photo.id,
    // large2x ~1880px de large : bon compromis qualité/poids pour une couverture web.
    download: photo.src.large2x || photo.src.original || photo.src.large,
    photographer: photo.photographer,
    photographerUrl: photo.photographer_url,
    pageUrl: photo.url,
    width: photo.width,
    height: photo.height,
  }
}

async function downloadTo(url, dest) {
  const res = await fetch(url)
  if (!res.ok) throw new Error(`download ${res.status} ${res.statusText}`)
  const buf = Buffer.from(await res.arrayBuffer())
  await writeFile(dest, buf)
  return buf.length
}

async function loadCredits() {
  try {
    return JSON.parse(await readFile(CREDITS_FILE, 'utf8'))
  } catch {
    return {}
  }
}

async function main() {
  await mkdir(OUT_DIR, { recursive: true })
  const credits = await loadCredits()
  const slugs = Object.keys(QUERIES).filter((s) => !ONLY || ONLY.includes(s))

  let done = 0
  let skipped = 0
  for (const slug of slugs) {
    const dest = path.join(OUT_DIR, `${slug}.jpg`)
    if (!FORCE && (await exists(dest))) {
      skipped++
      console.log(`· ${slug} : déja présent (skip)`)
      continue
    }
    try {
      const best = await searchBest(QUERIES[slug])
      if (!best) {
        console.warn(`! ${slug} : aucun résultat pour « ${QUERIES[slug]} »`)
        continue
      }
      const bytes = await downloadTo(best.download, dest)
      credits[slug] = {
        source: 'Pexels',
        photographer: best.photographer,
        photographerUrl: best.photographerUrl,
        pageUrl: best.pageUrl,
        query: QUERIES[slug],
      }
      done++
      console.log(`✓ ${slug} : ${(bytes / 1024).toFixed(0)} Ko  (${best.photographer}, ${best.pageUrl})`)
      // Respect du rate-limit : petite pause entre les requêtes.
      await new Promise((r) => setTimeout(r, 400))
    } catch (err) {
      console.error(`✗ ${slug} : ${err.message}`)
    }
  }

  await writeFile(CREDITS_FILE, JSON.stringify(credits, null, 2) + '\n', 'utf8')
  console.log(`\nTerminé : ${done} téléchargée(s), ${skipped} ignorée(s). Crédits -> ${path.relative(process.cwd(), CREDITS_FILE)}`)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
