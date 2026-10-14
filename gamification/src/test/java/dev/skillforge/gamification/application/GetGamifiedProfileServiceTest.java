package dev.skillforge.gamification.application;

import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.model.Level;
import dev.skillforge.gamification.domain.port.out.LevelRepository;
import dev.skillforge.gamification.domain.port.out.XpLedger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

class GetGamifiedProfileServiceTest {

    private final List<Level> levels = List.of(
            new Level("BEGINNER", "Débutant", 0, 1),
            new Level("APPRENTICE", "Apprenti", 500, 2),
            new Level("PRACTITIONER", "Praticien", 1500, 3));

    private GetGamifiedProfileService service(long totalXp) {
        XpLedger ledger = new XpLedger() {
            @Override
            public Mono<Void> add(LearnerId l, int a, String r, String t, UUID s) {
                return Mono.empty();
            }

            @Override
            public Mono<Long> totalXp(LearnerId learner) {
                return Mono.just(totalXp);
            }
        };
        LevelRepository repo = () -> Flux.fromIterable(levels);
        return new GetGamifiedProfileService(ledger, repo);
    }

    @Test
    void niveau_courant_et_prochain_palier() {
        StepVerifier.create(service(700).get(new LearnerId(UUID.randomUUID())))
                .assertNext(p -> {
                    org.junit.jupiter.api.Assertions.assertEquals("APPRENTICE", p.currentLevel().code());
                    org.junit.jupiter.api.Assertions.assertEquals("PRACTITIONER", p.nextLevel().code());
                    org.junit.jupiter.api.Assertions.assertEquals(800, p.xpToNextLevel()); // 1500 - 700
                })
                .verifyComplete();
    }

    @Test
    void niveau_max_pas_de_palier_suivant() {
        StepVerifier.create(service(2000).get(new LearnerId(UUID.randomUUID())))
                .assertNext(p -> {
                    org.junit.jupiter.api.Assertions.assertEquals("PRACTITIONER", p.currentLevel().code());
                    org.junit.jupiter.api.Assertions.assertEquals(null, p.nextLevel());
                    org.junit.jupiter.api.Assertions.assertEquals(0, p.xpToNextLevel());
                })
                .verifyComplete();
    }
}
