package dev.skillforge.lab.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.common.event.PracticalPassedEvent;
import dev.skillforge.common.id.UuidV7;
import dev.skillforge.lab.domain.model.ExecResult;
import dev.skillforge.lab.domain.model.LabRuntimeSpec;
import dev.skillforge.lab.domain.model.LearnerId;
import dev.skillforge.lab.domain.model.PracticalAttempt;
import dev.skillforge.lab.domain.model.PracticalTask;
import dev.skillforge.lab.domain.model.RunningLab;
import dev.skillforge.lab.domain.port.in.PracticalVerdict;
import dev.skillforge.lab.domain.port.in.SubmitPracticalUseCase;
import dev.skillforge.lab.domain.port.in.SubmittedFile;
import dev.skillforge.lab.domain.port.out.LabEventPublisher;
import dev.skillforge.lab.domain.port.out.LabRuntimePort;
import dev.skillforge.lab.domain.port.out.PracticalAttemptStore;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Corrige une soumission pratique : démarre un conteneur durci, y dépose les fichiers de
 * l'apprenant, exécute le script de validation, en déduit le verdict, persiste la tentative,
 * publie {@link PracticalPassedEvent} si réussi, puis détruit le conteneur (toujours).
 *
 * <p>Tout l'isolement est porté par {@link LabRuntimePort} / {@code SandboxPolicy}. Sans démon
 * Docker accessible, {@code start} échoue proprement : la capacité reste inerte par défaut.
 */
public class SubmitPracticalService implements SubmitPracticalUseCase {

    private static final int MAX_OUTPUT = 4000;

    private final PracticalTaskStore tasks;
    private final PracticalAttemptStore attempts;
    private final LabRuntimePort runtime;
    private final LabEventPublisher events;

    public SubmitPracticalService(PracticalTaskStore tasks, PracticalAttemptStore attempts,
                                  LabRuntimePort runtime, LabEventPublisher events) {
        this.tasks = tasks;
        this.attempts = attempts;
        this.runtime = runtime;
        this.events = events;
    }

    @Override
    public Mono<PracticalVerdict> submit(LearnerId learner, UUID nodeId, List<SubmittedFile> files) {
        return tasks.findByNode(nodeId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "PRACTICAL_TASK_NOT_FOUND", "Ce nœud n'a pas d'épreuve pratique.")))
                .flatMap(task -> correct(learner, nodeId, task, files));
    }

    private Mono<PracticalVerdict> correct(LearnerId learner, UUID nodeId, PracticalTask task,
                                           List<SubmittedFile> files) {
        UUID attemptId = UuidV7.generate();
        Instant startedAt = Instant.now();
        PracticalAttempt pending = PracticalAttempt.pending(attemptId, learner, task.id(), nodeId, startedAt);
        LabRuntimeSpec spec = new LabRuntimeSpec(
                attemptId.toString(), task.image(), 1.0, 768, 256, task.timeoutSeconds());

        Mono<ExecResult> run = Mono.usingWhen(
                runtime.start(spec),
                lab -> seedThenValidate(lab, files, task),
                runtime::stop);

        return run.flatMap(result -> {
            Instant finishedAt = Instant.now();
            String output = truncate(merge(result));
            PracticalAttempt finished = result.passed()
                    ? pending.markPassed(output, finishedAt)
                    : pending.markFailed(output, finishedAt);
            Mono<Void> afterSave = result.passed()
                    ? events.publish(new PracticalPassedEvent(learner.value(), nodeId, task.id().value()))
                    : Mono.empty();
            return attempts.save(finished)
                    .then(afterSave)
                    .thenReturn(new PracticalVerdict(attemptId, result.passed(), output));
        });
    }

    private Mono<ExecResult> seedThenValidate(RunningLab lab, List<SubmittedFile> files, PracticalTask task) {
        return Flux.fromIterable(files)
                .concatMap(f -> runtime.putFile(lab, f.path(), f.content()))
                .then(runtime.exec(lab, List.of("bash", "-lc", task.validationRef())));
    }

    private static String merge(ExecResult r) {
        String err = r.stderr() == null ? "" : r.stderr();
        String out = r.stdout() == null ? "" : r.stdout();
        return err.isBlank() ? out : out + "\n" + err;
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() <= MAX_OUTPUT ? s : s.substring(0, MAX_OUTPUT) + "\n[...]";
    }
}
