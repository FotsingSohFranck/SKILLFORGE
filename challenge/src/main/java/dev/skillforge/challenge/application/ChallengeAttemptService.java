package dev.skillforge.challenge.application;

import dev.skillforge.challenge.domain.ChallengeEngine;
import dev.skillforge.challenge.domain.model.AttemptStatus;
import dev.skillforge.challenge.domain.model.AttemptView;
import dev.skillforge.challenge.domain.model.ChallengeResult;
import dev.skillforge.challenge.domain.model.Decision;
import dev.skillforge.challenge.domain.model.DecisionOutcome;
import dev.skillforge.challenge.domain.model.GuidanceLevel;
import dev.skillforge.challenge.domain.model.ScenarioNode;
import dev.skillforge.challenge.domain.model.SimulationState;
import dev.skillforge.challenge.domain.port.in.GetAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.ResumeAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.StartAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.SubmitDecisionUseCase;
import dev.skillforge.challenge.domain.port.out.AttemptStore;
import dev.skillforge.challenge.domain.port.out.ChallengeEventPublisher;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.common.event.FinalChallengePassedEvent;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestration des tentatives de Final Challenge : charge le scénario figé, délègue la logique pure
 * au {@link ChallengeEngine} et persiste via les stores. Le serveur fait foi (scoring côté serveur).
 */
public class ChallengeAttemptService
        implements StartAttemptUseCase, SubmitDecisionUseCase, GetAttemptUseCase, ResumeAttemptUseCase {

    private final ScenarioStore scenarioStore;
    private final AttemptStore attemptStore;
    private final ChallengeEventPublisher events;

    public ChallengeAttemptService(ScenarioStore scenarioStore, AttemptStore attemptStore,
                                   ChallengeEventPublisher events) {
        this.scenarioStore = scenarioStore;
        this.attemptStore = attemptStore;
        this.events = events;
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
                                    .map(attempt -> AttemptView.inProgress(
                                            attempt.id(), def, initial, null, List.of()));
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
                                List<Decision> all = new ArrayList<>(attempt.decisions());
                                all.add(decision);

                                if (outcome.terminal()) {
                                    ChallengeResult result = ChallengeEngine.score(def, all, outcome.state());
                                    Mono<Void> notifyIfPassed = result.passed()
                                            ? events.publish(new FinalChallengePassedEvent(
                                                    learnerId, attempt.nodeId(), attempt.scenario().scenarioId()))
                                            : Mono.empty();
                                    return attemptStore.complete(attemptId, decision, outcome.state(), result)
                                            .then(notifyIfPassed)
                                            .thenReturn(AttemptView.completed(
                                                    attempt.id(), def, result, ChallengeEngine.history(def, all)));
                                }
                                GuidanceLevel guidance = ChallengeEngine.guidance(def, all, outcome.state());
                                return attemptStore.advance(attemptId, decision, outcome.state(), outcome.nextNode().id())
                                        .thenReturn(AttemptView.inProgress(attempt.id(), def, outcome.nextNode(),
                                                guidance, ChallengeEngine.history(def, all)));
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
                        .map(def -> toView(attempt, def)));
    }

    @Override
    public Mono<AttemptView> currentForNode(UUID learnerId, UUID nodeId) {
        return attemptStore.findInProgressByLearnerAndNode(learnerId, nodeId)
                .flatMap(attempt -> scenarioStore
                        .findByRef(attempt.scenario().scenarioId(), attempt.scenario().version())
                        .map(def -> toView(attempt, def)));
    }

    /** Projette une tentative chargée (en cours ou terminée) en vue, historique inclus. */
    private AttemptView toView(dev.skillforge.challenge.domain.model.ChallengeAttempt attempt,
                               dev.skillforge.challenge.domain.model.ScenarioDefinition def) {
        var history = ChallengeEngine.history(def, attempt.decisions());
        if (attempt.status() == AttemptStatus.COMPLETED) {
            return AttemptView.completed(attempt.id(), def, attempt.result(), history);
        }
        return AttemptView.inProgress(attempt.id(), def,
                ChallengeEngine.nodeById(def, attempt.currentNodeId()),
                ChallengeEngine.guidance(def, attempt.decisions(), attempt.state()), history);
    }

    private Mono<dev.skillforge.challenge.domain.model.ChallengeAttempt> ownedAttempt(UUID attemptId, UUID learnerId) {
        return attemptStore.findOwned(attemptId, learnerId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "ATTEMPT_NOT_FOUND", "Tentative introuvable.")));
    }
}
