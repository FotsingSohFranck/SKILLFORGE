package dev.skillforge.challenge.domain.model;

/**
 * Indicateur directionnel montré pendant la simulation : une <strong>tendance</strong> sur la
 * trajectoire parcourue, jamais la correction du prochain choix (SF-92b, AC-08/AC-09).
 */
public enum GuidanceLevel {
    FAVORABLE,
    A_SURVEILLER,
    DEFAVORABLE
}
