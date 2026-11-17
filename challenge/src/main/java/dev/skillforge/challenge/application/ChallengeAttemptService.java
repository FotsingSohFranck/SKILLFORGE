package dev.skillforge.challenge.application;

import dev.skillforge.challenge.domain.ChallengeEngine;
import dev.skillforge.challenge.domain.model.AttemptStatus;
import dev.skillforge.challenge.domain.model.AttemptView;
import dev.skillforge.challenge.domain.model.ChallengeResult;
import dev.skillforge.challenge.domain.model.Decision;
import dev.skillforge.challenge.domain.model.DecisionOutcome;
import dev.skillforge.challenge.domain.model.ScenarioNode;
import dev.skillforge.challenge.domain.model.SimulationState;
import dev.skillforge.challenge.domain.port.in.GetAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.StartAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.SubmitDecisionUseCase;
import dev.skillforge.challenge.domain.port.out.AttemptStore;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestration des tentatives de Final Challenge : charge le scénario figé, délègue la logique pure
 * au {@link ChallengeEngine} et persiste via les stores. Le serveur fait foi (scoring côté serveur).
 */
public class ChallengeAttemptService implements StartAttemptUseCase, SubmitDecisionUseCase, GetAttemptUseCase {

    private final ScenarioStore scenarioStore;
    private final AttemptStore attemptStore;

    public ChallengeAttemptService(ScenarioStore scenarioStore, AttemptStore attemptStore) {
        this.scenarioStore = scenarioStore;
        this.attemptStore = attemptStore;
    }

    @Override
    public Mono<AttemptView> start(UUID learnerId, UUID nodeId) {
        return scenarioStore.findLatestPublishedRefByNode(nodeId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "SCENARIO_NOT_FOUND", "Aucun scénario de Final Challenge publié pour ce nœud.")))
                .flatMap(ref -> scenarioStore.findByRef(ref.scenarioId(), ref.version())
                        .flatMap(def -> {
                            ScenarioNode initial = ChallengeEngine.initialNode(def);
                            return attemptStore.create(ref.scenarioId(), ref.version(), nodeId, learnerId,
                                            initial.id(), SimulationState.empty())
                                    .map(attempt -> AttemptView.inProgress(attempt.id(), def, initial));
                        }));
    }

    @Override
    public Mono<AttemptView> submit(UUID learnerId, UUID attemptId, String choiceId) {
        return ownedAttempt(attemptId, learnerId)
                .flatMap(attempt -> {
                    if (attempt.status() == AttemptStatus.COMPLETED) {
                        return Mono.error(new BusinessRuleException(
                                "ATTEMPT_COMPLETED", "Cette tentative est déjà terminée."));
                    }
                    return scenarioStore.findByRef(attempt.scenario().scenarioId(), attempt.scenario().version())
                            .switchIfEmpty(Mono.error(new NotFoundException(
                                    "SCENARIO_NOT_FOUND", "Scénario de la tentative introuvable.")))
                            .flatMap(def -> {
                                DecisionOutcome outcome = ChallengeEngine.decide(
                                        def, attempt.state(), attempt.currentNodeId(), choiceId);
                                Decision decision = new Decision(
                                        attempt.decisions().size() + 1, attempt.currentNodeId(), choiceId);

                                if (outcome.terminal()) {
                                    List<Decision> all = new ArrayList<>(attempt.decisions());
                                    all.add(decision);
                                    ChallengeResult result = ChallengeEngine.score(def, all, outcome.state());
                                    return attemptStore.complete(attemptId, decision, outcome.state(), result)
                                            .thenReturn(AttemptView.completed(attempt.id(), def, result));
                                }
                                return attemptStore.advance(attemptId, decision, outcome.state(), outcome.nextNode().id())
                                        .thenReturn(AttemptView.inProgress(attempt.id(), def, outcome.nextNode()));
                            });
                });
    }

    @Override
    public Mono<AttemptView> get(UUID learnerId, UUID attemptId) {
        return ownedAttempt(attemptId, learnerId)
                .flatMap(attempt -> scenarioStore
                        .findByRef(attempt.scenario().scenarioId(), attempt.scenario().version())
                        .switchIfEmpty(Mono.error(new NotFoundException(
                                "SCENARIO_NOT_FOUND", "Scénario de la tentative introuvable.")))
                        .map(def -> attempt.status() == AttemptStatus.COMPLETED
                                ? AttemptView.completed(attempt.id(), def, attempt.result())
                                : AttemptView.inProgress(attempt.id(), def,
                                        ChallengeEngine.nodeById(def, attempt.currentNodeId()))));
    }

    private Mono<dev.skillforge.challenge.domain.model.ChallengeAttempt> ownedAttempt(UUID attemptId, UUID learnerId) {
        return attemptStore.findOwned(attemptId, learnerId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "ATTEMPT_NOT_FOUND", "Tentative introuvable.")));
    }
}
