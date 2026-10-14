package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.model.SubmissionResult;
import reactor.core.publisher.Mono;

/** Soumettre une solution : valide par comparaison aux points clés (sans exécution serveur). */
public interface SubmitChallengeUseCase {
    Mono<SubmissionResult> submit(ChallengeId challenge, String code);
}
