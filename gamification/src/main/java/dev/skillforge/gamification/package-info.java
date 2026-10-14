/**
 * Contexte borné <b>Gamification</b> (XP, niveaux nommés, badges, règles orientées événements).
 *
 * <p>Architecture hexagonale (cf. docs/ARCHITECTURE.md §2) : {@code domain},
 * {@code application}, {@code adapter.in.web}, {@code adapter.out.persistence},
 * {@code adapter.in.event} (consommation des événements d'apprentissage/évaluation).
 *
 * <p>TODO <b>SF-24</b> : attribution XP/niveaux/badges, profil gamifié.
 */
package dev.skillforge.gamification;
