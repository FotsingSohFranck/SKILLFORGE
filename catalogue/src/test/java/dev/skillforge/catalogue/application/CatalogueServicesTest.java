package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.Node;
import dev.skillforge.catalogue.domain.model.NodeId;
import dev.skillforge.catalogue.domain.model.NodeImportance;
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

class CatalogueServicesTest {

    @Test
    void getNode_renvoie_NotFound_si_absent() {
        NodeRepository nodes = new NodeRepository() {
            @Override
            public Flux<Node> findByRoadmapId(RoadmapId roadmapId) {
                return Flux.empty();
            }

            @Override
            public Mono<Node> findById(NodeId id) {
                return Mono.empty();
            }
        };
        var service = new GetNodeService(nodes);

        StepVerifier.create(service.getById(new NodeId(UUID.randomUUID())))
                .expectErrorMatches(e -> e instanceof NotFoundException nf
                        && nf.code().equals("NODE_NOT_FOUND"))
                .verify();
    }

    @Test
    void getNode_renvoie_le_noeud_si_present() {
        NodeId id = new NodeId(UUID.randomUUID());
        Node node = new Node(id, new RoadmapId(UUID.randomUUID()), null,
                "caching", "Caching", null, NodeImportance.RECOMMENDED, 75, 1);
        NodeRepository nodes = new NodeRepository() {
            @Override
            public Flux<Node> findByRoadmapId(RoadmapId roadmapId) {
                return Flux.empty();
            }

            @Override
            public Mono<Node> findById(NodeId nodeId) {
                return Mono.just(node);
            }
        };

        StepVerifier.create(new GetNodeService(nodes).getById(id))
                .assertNext(n -> org.junit.jupiter.api.Assertions.assertEquals("caching", n.slug()))
                .verifyComplete();
    }

    @Test
    void search_avec_requete_vide_retombe_sur_la_liste_complete() {
        var counter = new int[]{0, 0}; // [findAllOrdered, search]
        RoadmapRepository roadmaps = new RoadmapRepository() {
            @Override
            public Flux<Roadmap> findAllOrdered() {
                counter[0]++;
                return Flux.just(new Roadmap(new RoadmapId(UUID.randomUUID()),
                        "backend", "Backend", null, RoadmapKind.TRACK, 0));
            }

            @Override
            public Mono<Roadmap> findBySlug(String slug) {
                return Mono.empty();
            }

            @Override
            public Flux<Roadmap> search(String query) {
                counter[1]++;
                return Flux.empty();
            }
        };
        var service = new SearchRoadmapsService(roadmaps);

        StepVerifier.create(service.search("   ")).expectNextCount(1).verifyComplete();
        org.junit.jupiter.api.Assertions.assertEquals(1, counter[0], "findAllOrdered attendu");
        org.junit.jupiter.api.Assertions.assertEquals(0, counter[1], "search non attendu sur requête vide");
    }
}
