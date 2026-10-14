package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.port.in.RoadmapImport;
import dev.skillforge.catalogue.domain.port.out.RoadmapImporter;
import dev.skillforge.common.error.BusinessRuleException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

class IngestRoadmapServiceTest {

    private final AtomicInteger importCalls = new AtomicInteger();
    private final RoadmapImporter importer = command -> {
        importCalls.incrementAndGet();
        return Mono.just(new RoadmapId(UUID.randomUUID()));
    };
    private final IngestRoadmapService service = new IngestRoadmapService(importer);

    private RoadmapImport valid() {
        return new RoadmapImport("demo", "Démo", null, "TRACK", 0, null,
                List.of(new RoadmapImport.Phase("Base", 0)),
                List.of(
                        new RoadmapImport.Node("intro", "Intro", null, "Base", "CORE", 10, 0, null),
                        new RoadmapImport.Node("suite", "Suite", null, "Base", null, null, 1, null)),
                List.of(new RoadmapImport.Edge("intro", "suite", "PREREQ")));
    }

    @Test
    void import_valide_delegue_a_l_importer() {
        StepVerifier.create(service.ingest(valid())).expectNextCount(1).verifyComplete();
        org.junit.jupiter.api.Assertions.assertEquals(1, importCalls.get());
    }

    @Test
    void rejette_slug_manquant() {
        RoadmapImport bad = new RoadmapImport(" ", "Démo", null, "TRACK", 0, null,
                List.of(), List.of(), List.of());
        expectInvalid(bad);
    }

    @Test
    void rejette_slug_de_noeud_en_double() {
        RoadmapImport bad = new RoadmapImport("demo", "Démo", null, "TRACK", 0, null,
                List.of(),
                List.of(
                        new RoadmapImport.Node("dup", "A", null, null, null, null, 0, null),
                        new RoadmapImport.Node("dup", "B", null, null, null, null, 1, null)),
                List.of());
        expectInvalid(bad);
    }

    @Test
    void rejette_arete_vers_noeud_inconnu() {
        RoadmapImport bad = new RoadmapImport("demo", "Démo", null, "TRACK", 0, null,
                List.of(),
                List.of(new RoadmapImport.Node("a", "A", null, null, null, null, 0, null)),
                List.of(new RoadmapImport.Edge("a", "inconnu", "PREREQ")));
        expectInvalid(bad);
    }

    @Test
    void rejette_phase_inconnue_referencee_par_un_noeud() {
        RoadmapImport bad = new RoadmapImport("demo", "Démo", null, "TRACK", 0, null,
                List.of(new RoadmapImport.Phase("Base", 0)),
                List.of(new RoadmapImport.Node("a", "A", null, "Autre", null, null, 0, null)),
                List.of());
        expectInvalid(bad);
    }

    @Test
    void rejette_certification_sans_bloc() {
        RoadmapImport bad = new RoadmapImport("cka", "CKA", null, "CERTIFICATION", 0, null,
                List.of(), List.of(), List.of());
        expectInvalid(bad);
    }

    @Test
    void rejette_importance_invalide() {
        RoadmapImport bad = new RoadmapImport("demo", "Démo", null, "TRACK", 0, null,
                List.of(),
                List.of(new RoadmapImport.Node("a", "A", null, null, "SUPER", null, 0, null)),
                List.of());
        expectInvalid(bad);
    }

    private void expectInvalid(RoadmapImport bad) {
        StepVerifier.create(service.ingest(bad))
                .expectErrorMatches(e -> e instanceof BusinessRuleException b
                        && b.code().equals("INVALID_IMPORT"))
                .verify();
    }
}
