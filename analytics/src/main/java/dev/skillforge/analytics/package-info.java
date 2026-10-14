/**
 * Contexte borné <b>Analytics</b> (tableaux de bord, couverture des domaines,
 * recommandations, agrégats en lecture seule sur les autres contextes).
 *
 * <p>Architecture hexagonale (cf. docs/ARCHITECTURE.md §2) : {@code domain},
 * {@code application}, {@code adapter.in.web}, {@code adapter.out.persistence}.
 *
 * <p>TODO (Lot 3+) : dashboards, recommandations de révision.
 */
package dev.skillforge.analytics;
