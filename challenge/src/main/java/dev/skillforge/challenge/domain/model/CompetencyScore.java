package dev.skillforge.challenge.domain.model;

/**
 * Score d'une compétence : points obtenus ({@code raw}) sur points maximum atteignables le long du
 * chemin parcouru ({@code max}), et le pourcentage borné [0,100] qui en découle.
 */
public record CompetencyScore(String competency, int raw, int max, int percent) {
}
