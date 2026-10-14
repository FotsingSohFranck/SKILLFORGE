# ADR-0007 : Direction visuelle de l'UI (typographie, élévation, tokens)

## Statut

Accepté — 2026-07-21

## Contexte

Un audit UI « craft » (direction Linear/Apple) a montré que le design system, bien que
sain et tokenisé, restait dans une zone « propre mais générique » : police système,
neutres slate standards, séparation par bordures 1px, titres sans tracking, hovers
hétérogènes. Deux décisions structurantes portaient l'essentiel du gain de qualité perçue
(typographie, profondeur). Le Product Owner a demandé de les mettre en œuvre.

## Décision

**Typographie**
- Adoption d'**Inter** (variable, **auto-hébergée** via `@fontsource-variable/inter`),
  pour un rendu identique sur toutes les plateformes ; repli système conservé.
- **Échelle modulaire ~1.2** (`--text-lg 1.2` · `--text-xl 1.44` · `--text-2xl 1.728`)
  + palier **display** `--text-3xl 2.074` pour les hero (ex. titre d'accueil).
- **Tracking** négatif aux grandes tailles (tokens `--tracking-tight/-snug`), appliqué
  aux titres.

**Profondeur & couleur (parti pris assumé)**
- Les **cartes** passent de « contour 1px » à **élévation par l'ombre** (fin de l'effet
  « boîte »). Ombres rendues plus **diffuses** (`--shadow-*`).
- **Neutres légèrement tempérés** d'un soupçon de violet (gris « choisi » plutôt que slate
  par défaut), en préservant les contrastes AA.
- **Callouts** allégés : barre d'accent gauche plutôt qu'un contour saturé.
- **Overlay** de modale tokenisé (`--color-overlay`) + `backdrop-filter: blur(4px)`.

**Cohérence des états & tokens**
- Hovers `secondary`/`danger` via **tokens** `--color-*-hover` (fin du `filter: brightness`),
  + état `:active` (pressé) commun.
- La « question courante » de l'examen reçoit une **identité propre** (fond plein) au lieu
  de réutiliser la couleur de focus (fin de la collision sémantique avec l'anneau de focus).
- Espacements : ajout `--space-05` (2px) et `--space-7` (40px) ; valeurs magiques 2px
  tokenisées.

## Conséquences

- **+** Rendu typographique unifié et plus soigné (tracking, échelle harmonieuse, display).
- **+** Hiérarchie visuelle par l'élévation, moins « boîtes contourées » ; UI plus premium.
- **+** États cohérents (hover/pressé/focus) passant tous par le système de tokens.
- **−** Dépendance runtime ajoutée (fichiers de police Inter, ~quelques centaines de ko,
  auto-hébergés).
- **−** Changement visuel **transverse** : à valider sur les écrans clés (clair **et**
  sombre) après déploiement — les neutres tempérés et l'élévation touchent toutes les pages.
- Réversible : l'ensemble passe par les tokens ; revenir en arrière = ré-éditer `tokens.css`.
