package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.Edge;
import dev.skillforge.catalogue.domain.model.EdgeId;
import dev.skillforge.catalogue.domain.model.EdgeKind;
import dev.skillforge.catalogue.domain.model.Node;
import dev.skillforge.catalogue.domain.model.NodeId;
import dev.skillforge.catalogue.domain.model.NodeImportance;
import dev.skillforge.catalogue.domain.model.Phase;
import dev.skillforge.catalogue.domain.model.PhaseId;
import dev.skillforge.catalogue.domain.model.Roadmap;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.model.RoadmapKind;
import dev.skillforge.catalogue.domain.port.out.NodeRepository;
import dev.skillforge.catalogue.domain.port.out.RoadmapRepository;
import dev.skillforge.common.error.NotFoundException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetRoadmapGraphServiceTest {

    private final RoadmapId roadmapId = new RoadmapId(UUID.randomUUID());
    private final NodeId n1 = new NodeId(UUID.randomUUID());
    private final NodeId n2 = new NodeId(UUID.randomUUID());
    private final Roadmap backend = new Roadmap(
            roadmapId, "backend", "Backend Developer", "Démo", RoadmapKind.TRACK, 0);

    @Test
    void assemble_le_graphe_complet_pour_un_slug_existant() {
        var service = new GetRoadmapGraphService(
                roadmapRepo(Mono.just(backend)),
                id -> Flux.just(new Phase(new PhaseId(UUID.randomUUID()), "Fondamentaux", 0)),
                nodeRepo(Flux.just(node(n1, "internet"), node(n2, "git"))),
                id -> Flux.just(new Edge(new EdgeId(UUID.randomUUID()), n1, n2, EdgeKind.PREREQ)));

        StepVerifier.create(service.getBySlug("backend"))
                .assertNext(graph -> {
                    assertEquals("backend", graph.roadmap().slug());
                    assertEquals(1, graph.phases().size());
                    assertEquals(2, graph.nodes().size());
                    assertEquals(1, graph.edges().size());
                })
                .verifyComplete();
    }

    @Test
    void renvoie_NotFound_pour_un_slug_inconnu() {
        var service = new GetRoadmapGraphService(
                roadmapRepo(Mono.empty()),
                id -> Flux.empty(), nodeRepo(Flux.empty()), id -> Flux.empty());

        StepVerifier.create(service.getBySlug("inconnu"))
                .expectErrorMatches(e -> e instanceof NotFoundException nf
                        && nf.code().equals("ROADMAP_NOT_FOUND"))
                .verify();
    }

    private Node node(NodeId id, String slug) {
        return new Node(id, roadmapId, null, slug, slug, null, NodeImportance.CORE, 60, 0);
    }

    /** {@link NodeRepository} a deux méthodes : fake explicite, {@code findByRoadmapId} paramétrable. */
    private NodeRepository nodeRepo(Flux<Node> byRoadmap) {
        return new NodeRepository() {
            @Override
            public Flux<Node> findByRoadmapId(RoadmapId id) {
                return byRoadmap;
            }

            @Override
            public Mono<Node> findById(NodeId id) {
                return Mono.empty();
            }
        };
    }

    /** {@link RoadmapRepository} a plusieurs méthodes : fake explicite, {@code findBySlug} paramétrable. */
    private RoadmapRepository roadmapRepo(Mono<Roadmap> bySlug) {
        return new RoadmapRepository() {
            @Override
            public Flux<Roadmap> findAllOrdered() {
                return Flux.empty();
            }

            @Override
            public Mono<Roadmap> findBySlug(String slug) {
                return bySlug;
            }

            @Override
            public Flux<Roadmap> search(String query) {
                return Flux.empty();
            }
        };
    }
}
