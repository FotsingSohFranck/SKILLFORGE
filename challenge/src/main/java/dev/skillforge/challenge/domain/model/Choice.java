package dev.skillforge.challenge.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Action/réponse proposée à un nœud. Porte les <strong>scores par compétence</strong> attribués
 * si elle est retenue, les <strong>effets</strong> sur l'état de simulation, et une justification
 * (affichée dans le bilan, pas pendant la mission).
 */
public record Choice(
        String id,
        String label,
        Map<String, Integer> competencyScores,
        List<Effect> effects,
        String rationale) {
}
