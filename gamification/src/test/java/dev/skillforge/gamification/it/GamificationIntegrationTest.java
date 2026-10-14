package dev.skillforge.gamification.it;

import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.in.AwardXpUseCase;
import dev.skillforge.gamification.domain.port.in.BadgeAwardingUseCase;
import dev.skillforge.gamification.domain.port.in.GetBadgesUseCase;
import dev.skillforge.gamification.domain.port.in.GetGamifiedProfileUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'intégration Gamification : attribution d'XP puis calcul du niveau (barème seedé)
 * contre PostgreSQL 18 (Testcontainers).
 */
@SpringBootTest(classes = GamificationITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class GamificationIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/gamification-it.sql");

    @DynamicPropertySource
    static void r2dbcProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort()
                + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    @Autowired AwardXpUseCase award;
    @Autowired GetGamifiedProfileUseCase profile;
    @Autowired BadgeAwardingUseCase badges;
    @Autowired GetBadgesUseCase getBadges;

    @Test
    void attribution_xp_puis_niveau_et_prochain_palier() {
        LearnerId learner = new LearnerId(UUID.randomUUID());

        // 50 (leçon) + 100 (nœud) + 500 (parcours) = 650 XP -> palier APPRENTICE (>=500).
        award.award(learner, 50, "LESSON_COMPLETED", "LESSON", UUID.randomUUID()).block();
        award.award(learner, 100, "NODE_COMPLETED", "NODE", UUID.randomUUID()).block();
        award.award(learner, 500, "ROADMAP_COMPLETED", "ROADMAP", UUID.randomUUID()).block();

        var p = profile.get(learner).block();
        assertEquals(650, p.totalXp());
        assertEquals("APPRENTICE", p.currentLevel().code());
        assertEquals("PRACTITIONER", p.nextLevel().code());
        assertEquals(850, p.xpToNextLevel()); // 1500 - 650
    }

    @Test
    void badges_attribues_sur_evenements_et_seuils() {
        LearnerId learner = new LearnerId(UUID.randomUUID());

        // Nœud complété -> badge NODE_MASTER.
        badges.onNodeCompleted(learner).block();
        // XP sous le seuil -> pas encore XP_1000.
        award.award(learner, 600, "X", "OTHER", null).block();
        badges.onXpChanged(learner).block();
        long beforeThreshold = getBadges.get(learner).collectList().block().size();
        org.junit.jupiter.api.Assertions.assertEquals(1, beforeThreshold, "seul NODE_MASTER");

        // Franchir 1000 XP -> badge XP_1000 attribué.
        award.award(learner, 500, "X", "OTHER", null).block(); // total 1100
        badges.onXpChanged(learner).block();
        var codes = getBadges.get(learner).collectList().block().stream().map(b -> b.code()).toList();
        assertTrue(codes.contains("NODE_MASTER"));
        assertTrue(codes.contains("XP_1000"));

        // Idempotence : réévaluer n'ajoute pas de doublon.
        badges.onNodeCompleted(learner).block();
        assertEquals(2, getBadges.get(learner).collectList().block().size());
    }
}
