package dev.skillforge.lab.application;

import dev.skillforge.lab.domain.model.LearnerId;
import dev.skillforge.lab.domain.model.PracticalAttempt;
import dev.skillforge.lab.domain.port.in.GetPracticalUseCase;
import dev.skillforge.lab.domain.port.in.PracticalView;
import dev.skillforge.lab.domain.port.out.LabCatalogPort;
import dev.skillforge.lab.domain.port.out.PracticalAttemptStore;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/** Consulte l'épreuve pratique d'un nœud et l'état de la dernière tentative de l'apprenant. */
public class GetPracticalService implements GetPracticalUseCase {

    private final PracticalTaskStore tasks;
    private final PracticalAttemptStore attempts;
    private final LabCatalogPort catalog;

    public GetPracticalService(PracticalTaskStore tasks, PracticalAttemptStore attempts, LabCatalogPort catalog) {
        this.tasks = tasks;
        this.attempts = attempts;
        this.catalog = catalog;
    }

    @Override
    public Mono<PracticalView> get(LearnerId learner, UUID nodeId) {
        return tasks.findByNode(nodeId)
                .flatMap(task -> Mono.zip(
                                attempts.findLatest(learner, nodeId).map(Optional::of).defaultIfEmpty(Optional.empty()),
                                catalog.requiresPractical(nodeId))
                        .map(t -> {
                            Optional<PracticalAttempt> latest = t.getT1();
                            boolean required = t.getT2();
                            String status = latest.map(a -> a.status().name()).orElse(null);
                            boolean passed = latest.map(PracticalAttempt::passed).orElse(false);
                            return PracticalView.present(required, task.title(), task.instructions(),
                                    task.expectedPath(), status, passed);
                        }))
                .switchIfEmpty(catalog.requiresPractical(nodeId).map(PracticalView::absent));
    }
}
