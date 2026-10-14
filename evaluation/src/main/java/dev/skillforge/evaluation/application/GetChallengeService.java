package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.model.CodeChallenge;
import dev.skillforge.evaluation.domain.port.in.GetChallengeUseCase;
import dev.skillforge.evaluation.domain.port.out.ChallengeRepository;
import reactor.core.publisher.Mono;

public class GetChallengeService implements GetChallengeUseCase {

    private final ChallengeRepository challenges;

    public GetChallengeService(ChallengeRepository challenges) {
        this.challenges = challenges;
    }

    @Override
    public Mono<CodeChallenge> get(ChallengeId challenge) {
        return challenges.findById(challenge)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "CHALLENGE_NOT_FOUND", "Défi introuvable : " + challenge.value())));
    }
}
