package dev.skillforge.apprentissage.it;

import dev.skillforge.common.event.NodeCompletedEvent;
import dev.skillforge.apprentissage.domain.model.BookmarkTarget;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.NodeProgress;
import dev.skillforge.apprentissage.domain.model.ProgressStatus;
import dev.skillforge.apprentissage.domain.model.ResumePointer;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.model.RoadmapProgress;
import dev.skillforge.apprentissage.domain.model.Streak;
import dev.skillforge.apprentissage.domain.port.in.CompleteLessonUseCase;
import dev.skillforge.apprentissage.domain.port.in.EnrollLearnerUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetNodeLessonProgressUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetRoadmapProgressUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetStreakUseCase;
import dev.skillforge.apprentissage.domain.port.in.ListBookmarksUseCase;
import dev.skillforge.apprentissage.domain.port.in.ListEnrollmentsUseCase;
import dev.skillforge.apprentissage.domain.port.in.MarkLessonViewedUseCase;
import dev.skillforge.apprentissage.domain.port.in.ResumeLastUseCase;
import dev.skillforge.apprentissage.domain.port.in.ToggleBookmarkUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'intégration du cœur Apprentissage contre PostgreSQL 18 (Testcontainers) :
 * scénario quitter-un-nœud / y-revenir (reprise EXACTE) + reprise GLOBALE + calcul des %.
 */
@SpringBootTest(classes = LearningITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class LearningIntegrationTest {

    private static final RoadmapId ROADMAP = new RoadmapId(UUID.fromString("019a0000-0000-7000-8000-0000000000a1"));
    private static final NodeId CACHING = new NodeId(UUID.fromString("019a0000-0000-7000-8000-0000000000b1"));
    private static final NodeId SQL = new NodeId(UUID.fromString("019a0000-0000-7000-8000-0000000000b2"));
    private static final LessonId L1 = new LessonId(UUID.fromString("019a0000-0000-7000-8000-0000000000c1"));
    private static final LessonId L2 = new LessonId(UUID.fromString("019a0000-0000-7000-8000-0000000000c2"));
    private static final LessonId L3 = new LessonId(UUID.fromString("019a0000-0000-7000-8000-0000000000c3"));

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/learning-it.sql");

    @DynamicPropertySource
    static void r2dbcProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort()
                + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    @Autowired EnrollLearnerUseCase enroll;
    @Autowired MarkLessonViewedUseCase markViewed;
    @Autowired CompleteLessonUseCase complete;
    @Autowired GetRoadmapProgressUseCase getProgress;
    @Autowired GetNodeLessonProgressUseCase getNodeLessons;
    @Autowired ResumeLastUseCase resumeLast;
    @Autowired ToggleBookmarkUseCase toggleBookmark;
    @Autowired ListBookmarksUseCase listBookmarks;
    @Autowired ListEnrollmentsUseCase listEnrollments;
    @Autowired GetStreakUseCase getStreak;
    @Autowired CapturingLearningEventListener eventListener;

    @Test
    void reprise_exacte_par_noeud_et_reprise_globale() {
        LearnerId learner = new LearnerId(UUID.randomUUID());

        enroll.enroll(learner, ROADMAP).block();
        markViewed.markViewed(learner, ROADMAP, CACHING, L1).block(); // ouvre caching, leçon 1
        markViewed.markViewed(learner, ROADMAP, SQL, L3).block();     // part sur un autre nœud
        markViewed.markViewed(learner, ROADMAP, CACHING, L1).block(); // revient sur caching

        RoadmapProgress progress = getProgress.get(learner, ROADMAP).block();
        assertNotNull(progress);
        NodeProgress caching = nodeOf(progress, CACHING);
        // Reprise EXACTE : la dernière leçon consultée dans "caching" est bien L1.
        assertEquals(L1.value(), caching.lastLessonId().value());

        // Reprise GLOBALE : dernier parcours actif -> caching / L1.
        ResumePointer resume = resumeLast.resume(learner).block();
        assertNotNull(resume);
        assertEquals(ROADMAP.value(), resume.roadmapId().value());
        assertEquals(CACHING.value(), resume.nodeId().value());
        assertEquals(L1.value(), resume.lessonId().value());
    }

    @Test
    void completion_calcule_les_pourcentages_de_noeud_et_de_parcours() {
        LearnerId learner = new LearnerId(UUID.randomUUID());

        // Complète les 2 leçons du nœud "caching" (sur 2) -> nœud à 100 %, COMPLETED.
        complete.complete(learner, ROADMAP, CACHING, L1).block();
        complete.complete(learner, ROADMAP, CACHING, L2).block();

        RoadmapProgress progress = getProgress.get(learner, ROADMAP).block();
        assertNotNull(progress);
        NodeProgress caching = nodeOf(progress, CACHING);
        assertEquals(ProgressStatus.COMPLETED, caching.status());
        assertEquals(100.0, caching.percent(), 0.01);

        // 1 nœud complété sur 2 dans le parcours -> 50 %.
        assertEquals(50.0, progress.enrollment().progressPercent(), 0.01);
    }

    @Test
    void lecons_completees_du_noeud_pour_les_coches() {
        LearnerId learner = new LearnerId(UUID.randomUUID());

        complete.complete(learner, ROADMAP, CACHING, L1).block();
        // L2 seulement vue, pas complétée -> ne doit pas apparaître dans les coches.
        markViewed.markViewed(learner, ROADMAP, CACHING, L2).block();

        List<LessonId> completed = getNodeLessons.completedLessons(learner, CACHING).collectList().block();
        assertNotNull(completed);
        assertEquals(1, completed.size());
        assertTrue(completed.stream().anyMatch(l -> l.value().equals(L1.value())));
    }

    @Test
    void liste_les_inscriptions_de_l_apprenant() {
        LearnerId learner = new LearnerId(UUID.randomUUID());
        enroll.enroll(learner, ROADMAP).block();

        var enrollments = listEnrollments.list(learner).collectList().block();
        assertNotNull(enrollments);
        assertEquals(1, enrollments.size());
        assertEquals(ROADMAP.value(), enrollments.get(0).roadmapId().value());
    }

    @Test
    void favoris_bascule_et_liste() {
        LearnerId learner = new LearnerId(UUID.randomUUID());
        UUID target = UUID.randomUUID();

        Boolean added = toggleBookmark.toggle(learner, BookmarkTarget.NODE, target).block();
        assertEquals(Boolean.TRUE, added);
        assertEquals(1, listBookmarks.list(learner).collectList().block().size());

        Boolean removed = toggleBookmark.toggle(learner, BookmarkTarget.NODE, target).block();
        assertEquals(Boolean.FALSE, removed);
        assertEquals(0, listBookmarks.list(learner).collectList().block().size());
    }

    @Test
    void une_activite_demarre_la_serie() {
        LearnerId learner = new LearnerId(UUID.randomUUID());
        markViewed.markViewed(learner, ROADMAP, CACHING, L1).block();

        Streak streak = getStreak.get(learner).block();
        assertNotNull(streak);
        assertEquals(1, streak.currentLength());
    }

    @Test
    void completer_un_noeud_publie_un_evenement_NodeCompleted() {
        LearnerId learner = new LearnerId(UUID.randomUUID());
        eventListener.clear();

        complete.complete(learner, ROADMAP, CACHING, L1).block();
        complete.complete(learner, ROADMAP, CACHING, L2).block();

        long nodeCompleted = eventListener.events().stream()
                .filter(e -> e instanceof NodeCompletedEvent).count();
        assertEquals(1, nodeCompleted, "un événement NodeCompleted attendu quand le nœud passe à 100 %");
    }

    private static NodeProgress nodeOf(RoadmapProgress progress, NodeId node) {
        return progress.nodes().stream()
                .filter(n -> n.nodeId().value().equals(node.value()))
                .findFirst().orElseThrow();
    }
}
