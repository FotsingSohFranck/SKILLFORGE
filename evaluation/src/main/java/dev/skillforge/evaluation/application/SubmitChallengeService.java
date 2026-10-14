package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.model.CodeChallenge;
import dev.skillforge.evaluation.domain.model.SubmissionResult;
import dev.skillforge.evaluation.domain.port.in.SubmitChallengeUseCase;
import dev.skillforge.evaluation.domain.port.out.ChallengeRepository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Valide une soumission par <b>comparaison aux points clés</b> (sous-chaînes présentes,
 * insensible à la casse) — aucune exécution de code côté serveur. Réussite = tous les points
 * clés couverts.
 */
public class SubmitChallengeService implements SubmitChallengeUseCase {

    private final ChallengeRepository challenges;

    public SubmitChallengeService(ChallengeRepository challenges) {
        this.challenges = challenges;
    }

    @Override
    public Mono<SubmissionResult> submit(ChallengeId challenge, String code) {
        return challenges.findById(challenge)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "CHALLENGE_NOT_FOUND", "Défi introuvable : " + challenge.value())))
                .map(loaded -> evaluate(loaded, code));
    }

    private SubmissionResult evaluate(CodeChallenge challenge, String code) {
        String normalized = code == null ? "" : code.toLowerCase();
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String keyPoint : challenge.keyPoints()) {
            if (keyPoint != null && !keyPoint.isBlank()
                    && normalized.contains(keyPoint.toLowerCase().trim())) {
                matched.add(keyPoint);
            } else if (keyPoint != null && !keyPoint.isBlank()) {
                missing.add(keyPoint);
            }
        }
        boolean passed = missing.isEmpty() && !matched.isEmpty();
        return new SubmissionResult(passed, matched, missing, challenge.solution());
    }
}
