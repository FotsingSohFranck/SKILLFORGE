package dev.skillforge.evaluation.application;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.model.ChallengeSummary;
import dev.skillforge.evaluation.domain.model.CodeChallenge;
import dev.skillforge.evaluation.domain.port.out.ChallengeRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitChallengeServiceTest {

    private final ChallengeId id = new ChallengeId(UUID.randomUUID());
    private final ChallengeRepository challenges = new ChallengeRepository() {
        @Override
        public Mono<CodeChallenge> findById(ChallengeId c) {
            return Mono.just(new CodeChallenge(
                    id, "Défi", "Implémente un cache", "// TODO", "Map<String,String> cache = ...;",
                    List.of("ConcurrentHashMap", "computeIfAbsent"), "java"));
        }

        @Override
        public Flux<ChallengeSummary> findByNode(UUID nodeId) {
            return Flux.empty();
        }
    };

    @Test
    void reussite_si_tous_les_points_cles_presents() {
        var service = new SubmitChallengeService(challenges);
        String code = "var cache = new ConcurrentHashMap<String,String>(); cache.computeIfAbsent(k, f);";

        StepVerifier.create(service.submit(id, code))
                .assertNext(result -> {
                    assertTrue(result.passed());
                    assertEquals(2, result.matched().size());
                    assertTrue(result.missing().isEmpty());
                    assertEquals("Map<String,String> cache = ...;", result.solution());
                })
                .verifyComplete();
    }

    @Test
    void echec_si_un_point_cle_manque() {
        var service = new SubmitChallengeService(challenges);
        String code = "var cache = new ConcurrentHashMap<>();"; // manque computeIfAbsent

        StepVerifier.create(service.submit(id, code))
                .assertNext(result -> {
                    assertFalse(result.passed());
                    assertEquals(1, result.matched().size());
                    assertEquals(List.of("computeIfAbsent"), result.missing());
                })
                .verifyComplete();
    }
}
