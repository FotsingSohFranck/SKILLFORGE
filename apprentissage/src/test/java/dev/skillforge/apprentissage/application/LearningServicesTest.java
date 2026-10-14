package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.common.error.NotFoundException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

class LearningServicesTest {

    private final LearnerId learner = new LearnerId(UUID.randomUUID());
    private final RoadmapId roadmap = new RoadmapId(UUID.randomUUID());

    @Test
    void enroll_est_idempotent() {
        FakeEnrollmentStore store = new FakeEnrollmentStore();
        var service = new EnrollLearnerService(store);

        Enrollment first = service.enroll(learner, roadmap).block();
        Enrollment second = service.enroll(learner, roadmap).block();

        org.junit.jupiter.api.Assertions.assertNotNull(first);
        org.junit.jupiter.api.Assertions.assertEquals(first.id().value(), second.id().value());
        org.junit.jupiter.api.Assertions.assertEquals(1, store.createCount.get(), "une seule création");
    }

    @Test
    void get_progress_renvoie_404_si_non_inscrit() {
        FakeEnrollmentStore store = new FakeEnrollmentStore();
        var service = new GetRoadmapProgressService(store, null);

        StepVerifier.create(service.get(learner, roadmap))
                .expectErrorMatches(e -> e instanceof NotFoundException nf
                        && nf.code().equals("NOT_ENROLLED"))
                .verify();
    }

    /** Fake in-memory du port EnrollmentStore. */
    static final class FakeEnrollmentStore implements EnrollmentStore {
        final Map<String, Enrollment> byKey = new HashMap<>();
        final AtomicInteger createCount = new AtomicInteger();

        private String key(LearnerId l, RoadmapId r) {
            return l.value() + ":" + r.value();
        }

        @Override
        public Mono<Enrollment> findByLearnerAndRoadmap(LearnerId l, RoadmapId r) {
            return Mono.justOrEmpty(byKey.get(key(l, r)));
        }

        @Override
        public reactor.core.publisher.Flux<Enrollment> findByLearner(LearnerId l) {
            return reactor.core.publisher.Flux.fromIterable(byKey.values())
                    .filter(e -> e.learnerId().value().equals(l.value()));
        }

        @Override
        public Mono<Enrollment> create(LearnerId l, RoadmapId r) {
            return Mono.defer(() -> {
                createCount.incrementAndGet();
                Enrollment e = new Enrollment(new EnrollmentId(UUID.randomUUID()), l, r, 0, null, null, null);
                byKey.put(key(l, r), e);
                return Mono.just(e);
            });
        }

        @Override
        public Mono<Void> updateProgress(EnrollmentId id, NodeId n, LessonId l, double p, Instant at) {
            return Mono.empty();
        }

        @Override
        public Mono<Enrollment> findLastActive(LearnerId l) {
            return Mono.empty();
        }
    }
}
