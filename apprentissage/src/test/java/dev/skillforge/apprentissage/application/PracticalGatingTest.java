package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.CompletionPolicy;
import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.NodeProgress;
import dev.skillforge.apprentissage.domain.model.ProgressStatus;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.apprentissage.domain.port.out.LearningCatalogPort;
import dev.skillforge.apprentissage.domain.port.out.LearningEventPublisher;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import dev.skillforge.apprentissage.domain.port.out.NodeProgressStore;
import dev.skillforge.common.event.NodeCompletedEvent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticalGatingTest {

    private final LearnerId learner = new LearnerId(UUID.randomUUID());
    private final RoadmapId roadmap = new RoadmapId(UUID.randomUUID());
    private final NodeId node = new NodeId(UUID.randomUUID());
    private final LessonId lesson = new LessonId(UUID.randomUUID());
    private final Enrollment enrollment =
            new Enrollment(new EnrollmentId(UUID.randomUUID()), learner, roadmap, 0, null, null, null);

    private ProgressStatus recalc(CompletionPolicy policy, long done, long total, boolean practical) {
        FakeCatalog catalog = new FakeCatalog(policy, total, 10, roadmap);
        FakeNodeProgress np = new FakeNodeProgress(practical, ProgressStatus.IN_PROGRESS);
        FakeLessons lessons = new FakeLessons(done);
        var recalc = new ProgressRecalculator(new FakeEnrollments(), np, lessons, catalog);
        return recalc.recalculate(enrollment, node, lesson, Instant.now()).block().nodeStatus();
    }

    @Test
    void pratique_requise_non_reussie_bloque_la_validation_du_noeud() {
        assertEquals(ProgressStatus.IN_PROGRESS,
                recalc(CompletionPolicy.LESSONS_AND_PRACTICAL, 2, 2, false));
    }

    @Test
    void pratique_requise_et_reussie_valide_le_noeud() {
        assertEquals(ProgressStatus.COMPLETED,
                recalc(CompletionPolicy.LESSONS_AND_PRACTICAL, 2, 2, true));
    }

    @Test
    void politique_lessons_only_ignore_la_pratique() {
        assertEquals(ProgressStatus.COMPLETED,
                recalc(CompletionPolicy.LESSONS_ONLY, 2, 2, false));
    }

    @Test
    void reussir_la_pratique_quand_les_lecons_sont_finies_complete_le_noeud_et_emet_l_evenement() {
        FakeCatalog catalog = new FakeCatalog(CompletionPolicy.LESSONS_AND_PRACTICAL, 2, 10, roadmap);
        FakeNodeProgress np = new FakeNodeProgress(false, ProgressStatus.IN_PROGRESS);
        FakeEnrollments enr = new FakeEnrollments();
        enr.fixed = enrollment;
        FakeEvents events = new FakeEvents();
        var service = new MarkNodePracticalPassedService(enr, np, new FakeLessons(2), catalog, events);

        service.markPassed(learner.value(), node.value()).block();

        assertTrue(np.markedPractical);
        assertEquals(ProgressStatus.COMPLETED, np.lastUpdatedStatus);
        assertEquals(1, events.published.size());
        assertTrue(events.published.get(0) instanceof NodeCompletedEvent);
    }

    @Test
    void reussir_la_pratique_sans_toutes_les_lecons_ne_valide_pas_le_noeud() {
        FakeCatalog catalog = new FakeCatalog(CompletionPolicy.LESSONS_AND_PRACTICAL, 3, 10, roadmap);
        FakeNodeProgress np = new FakeNodeProgress(false, ProgressStatus.IN_PROGRESS);
        FakeEnrollments enr = new FakeEnrollments();
        enr.fixed = enrollment;
        FakeEvents events = new FakeEvents();
        var service = new MarkNodePracticalPassedService(enr, np, new FakeLessons(1), catalog, events);

        service.markPassed(learner.value(), node.value()).block();

        assertTrue(np.markedPractical);
        assertEquals(ProgressStatus.IN_PROGRESS, np.lastUpdatedStatus);
        assertFalse(events.published.stream().anyMatch(e -> e instanceof NodeCompletedEvent));
    }

    // --- doubles ---

    final class FakeCatalog implements LearningCatalogPort {
        private final CompletionPolicy policy;
        private final long lessonsInNode;
        private final long nodesInRoadmap;
        private final RoadmapId roadmapId;
        FakeCatalog(CompletionPolicy p, long lessons, long nodes, RoadmapId r) {
            this.policy = p; this.lessonsInNode = lessons; this.nodesInRoadmap = nodes; this.roadmapId = r;
        }
        public Mono<Long> countLessonsInNode(NodeId n) { return Mono.just(lessonsInNode); }
        public Mono<Long> countNodesInRoadmap(RoadmapId r) { return Mono.just(nodesInRoadmap); }
        public Mono<CompletionPolicy> completionPolicy(NodeId n) { return Mono.just(policy); }
        public Mono<RoadmapId> roadmapOfNode(NodeId n) { return Mono.just(roadmapId); }
    }

    static final class FakeLessons implements LessonProgressStore {
        private final long completed;
        FakeLessons(long completed) { this.completed = completed; }
        public Mono<Void> markViewed(EnrollmentId e, NodeId n, LessonId l, Instant at) { return Mono.empty(); }
        public Mono<Void> markCompleted(EnrollmentId e, NodeId n, LessonId l, Instant at) { return Mono.empty(); }
        public Mono<Long> countCompletedInNode(EnrollmentId e, NodeId n) { return Mono.just(completed); }
        public Flux<LessonId> findCompletedLessonIds(LearnerId l, NodeId n) { return Flux.empty(); }
    }

    static final class FakeNodeProgress implements NodeProgressStore {
        private final boolean practicalPassed;
        private final ProgressStatus current;
        boolean markedPractical = false;
        ProgressStatus lastUpdatedStatus;
        FakeNodeProgress(boolean practicalPassed, ProgressStatus current) {
            this.practicalPassed = practicalPassed; this.current = current;
        }
        public Mono<Void> upsert(EnrollmentId e, NodeId n, ProgressStatus s, double p, LessonId l, Instant at) {
            lastUpdatedStatus = s; return Mono.empty();
        }
        public Flux<NodeProgress> findByEnrollment(EnrollmentId e) { return Flux.empty(); }
        public Mono<Long> countCompleted(EnrollmentId e) { return Mono.just(0L); }
        public Mono<ProgressStatus> status(EnrollmentId e, NodeId n) { return Mono.just(current); }
        public Mono<Boolean> isPracticalPassed(EnrollmentId e, NodeId n) { return Mono.just(practicalPassed); }
        public Mono<Void> markPracticalPassed(EnrollmentId e, NodeId n, Instant at) {
            markedPractical = true; return Mono.empty();
        }
        public Mono<Void> updateStatus(EnrollmentId e, NodeId n, ProgressStatus s, double p, Instant at) {
            lastUpdatedStatus = s; return Mono.empty();
        }
    }

    static final class FakeEnrollments implements EnrollmentStore {
        Enrollment fixed;
        public Mono<Enrollment> findByLearnerAndRoadmap(LearnerId l, RoadmapId r) { return Mono.justOrEmpty(fixed); }
        public Flux<Enrollment> findByLearner(LearnerId l) { return Flux.empty(); }
        public Mono<Enrollment> create(LearnerId l, RoadmapId r) { return Mono.justOrEmpty(fixed); }
        public Mono<Void> updateProgress(EnrollmentId id, NodeId n, LessonId l, double p, Instant at) { return Mono.empty(); }
        public Mono<Void> updatePercent(EnrollmentId id, double p, Instant at) { return Mono.empty(); }
        public Mono<Enrollment> findLastActive(LearnerId l) { return Mono.empty(); }
    }

    static final class FakeEvents implements LearningEventPublisher {
        final List<Object> published = new ArrayList<>();
        public Mono<Void> publish(Object event) { published.add(event); return Mono.empty(); }
    }
}
