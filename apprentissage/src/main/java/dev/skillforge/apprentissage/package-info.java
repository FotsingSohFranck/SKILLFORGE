/**
 * Contexte borné <b>Apprentissage</b> (inscriptions, progression, navigation non linéaire,
 * reprise exacte, favoris, streaks). Cœur du produit.
 *
 * <p>Architecture hexagonale (cf. docs/ARCHITECTURE.md §2, patron de référence) :
 * {@code domain} (model, event, port.in, port.out), {@code application},
 * {@code adapter.in.web}, {@code adapter.out.persistence}, {@code adapter.out.event}.
 *
 * <p>TODO <b>SF-21</b> : Enroll/MarkLessonViewed/CompleteLesson/ResumeLast/ToggleBookmark.
 */
package dev.skillforge.apprentissage;
