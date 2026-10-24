package dev.skillforge.lab.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.common.event.PracticalPassedEvent;
import dev.skillforge.lab.domain.model.AttemptStatus;
import dev.skillforge.lab.domain.model.ExecResult;
import dev.skillforge.lab.domain.model.LabRuntimeSpec;
import dev.skillforge.lab.domain.model.LearnerId;
import dev.skillforge.lab.domain.model.PracticalAttempt;
import dev.skillforge.lab.domain.model.PracticalTask;
import dev.skillforge.lab.domain.model.PracticalTaskId;
import dev.skillforge.lab.domain.model.RunningLab;
import dev.skillforge.lab.domain.port.in.PracticalVerdict;
import dev.skillforge.lab.domain.port.in.SubmittedFile;
import dev.skillforge.lab.domain.port.out.LabEventPublisher;
import dev.skillforge.lab.domain.port.out.LabRuntimePort;
import dev.skillforge.lab.domain.port.out.PracticalAttemptStore;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitPracticalServiceTest {

    private final UUID nodeId = UUID.randomUUID();
    private final LearnerId learner = LearnerId.of(UUID.randomUUID());

    private PracticalTask task() {
        return new PracticalTask(PracticalTaskId.of(UUID.randomUUID()), nodeId,
                "Afficher Bonjour", "eclipse-temurin:21-jdk", "Ecris le code", null, "bash /grader/check.sh", 900,
                "src/main/java/forge/Solution.java");
    }

    @Test
    void reussite_persiste_passed_et_publie_l_evenement() {
        FakeAttempts attempts = new FakeAttempts();
        FakeEvents events = new FakeEvents();
        FakeRuntime runtime = new FakeRuntime(new ExecResult(0, "PRATIQUE_OK", ""));
        var service = new SubmitPracticalService(new FakeTasks(task()), attempts, runtime, events);

        PracticalVerdict v = service.submit(learner, nodeId,
                List.of(new SubmittedFile("src/Main.java", "class Main{}"))).block();

        assertTrue(v.passed());
        assertEquals(AttemptStatus.PASSED, attempts.saved.get(attempts.saved.size() - 1).status());
        assertEquals(1, events.published.size());
        assertTrue(events.published.get(0) instanceof PracticalPassedEvent);
        assertTrue(runtime.stopped.get(), "le conteneur doit etre detruit");
        assertEquals(1, runtime.written); // 1 fichier depose
    }

    @Test
    void echec_persiste_failed_sans_evenement() {
        FakeAttempts attempts = new FakeAttempts();
        FakeEvents events = new FakeEvents();
        FakeRuntime runtime = new FakeRuntime(new ExecResult(1, "", "tests rouges"));
        var service = new SubmitPracticalService(new FakeTasks(task()), attempts, runtime, events);

        PracticalVerdict v = service.submit(learner, nodeId,
                List.of(new SubmittedFile("src/Main.java", "class Main{}"))).block();

        assertFalse(v.passed());
        assertEquals(AttemptStatus.FAILED, attempts.saved.get(attempts.saved.size() - 1).status());
        assertTrue(events.published.isEmpty());
        assertTrue(runtime.stopped.get());
    }

    @Test
    void noeud_sans_epreuve_leve_not_found() {
        var service = new SubmitPracticalService(new FakeTasks(null), new FakeAttempts(),
                new FakeRuntime(new ExecResult(0, "", "")), new FakeEvents());
        assertThrows(NotFoundException.class,
                () -> service.submit(learner, nodeId, List.of(new SubmittedFile("a", "b"))).block());
    }

    // --- doubles ---

    private static final class FakeTasks implements PracticalTaskStore {
        private final PracticalTask task;
        FakeTasks(PracticalTask task) { this.task = task; }
        public Mono<PracticalTask> findByNode(UUID nodeId) { return task == null ? Mono.empty() : Mono.just(task); }
        public Mono<PracticalTask> findById(PracticalTaskId id) { return Mono.justOrEmpty(task); }
        public Mono<Void> upsert(PracticalTask t) { return Mono.empty(); }
    }

    private static final class FakeAttempts implements PracticalAttemptStore {
        final List<PracticalAttempt> saved = new ArrayList<>();
        public Mono<PracticalAttempt> save(PracticalAttempt a) { saved.add(a); return Mono.just(a); }
        public Mono<PracticalAttempt> findLatest(LearnerId l, UUID n) { return Mono.empty(); }
        public Mono<Boolean> hasPassed(LearnerId l, UUID n) { return Mono.just(false); }
    }

    private static final class FakeEvents implements LabEventPublisher {
        final List<Object> published = new ArrayList<>();
        public Mono<Void> publish(Object event) { published.add(event); return Mono.empty(); }
    }

    private static final class FakeRuntime implements LabRuntimePort {
        private final ExecResult result;
        final AtomicBoolean stopped = new AtomicBoolean(false);
        int written = 0;
        FakeRuntime(ExecResult result) { this.result = result; }
        public Mono<RunningLab> start(LabRuntimeSpec spec) { return Mono.just(new RunningLab("cid", spec)); }
        public Mono<Void> putFile(RunningLab lab, String path, String content) { written++; return Mono.empty(); }
        public Mono<ExecResult> exec(RunningLab lab, List<String> command) { return Mono.just(result); }
        public Mono<Void> stop(RunningLab lab) { return Mono.fromRunnable(() -> stopped.set(true)); }
        public Mono<Integer> reapExpired() { return Mono.just(0); }
    }
}
