package dev.skillforge.catalogue.adapter.in.web;

import dev.skillforge.catalogue.adapter.in.web.dto.CertificationDto;
import dev.skillforge.catalogue.adapter.in.web.dto.NodeDto;
import dev.skillforge.catalogue.adapter.in.web.dto.RoadmapGraphDto;
import dev.skillforge.catalogue.adapter.in.web.dto.RoadmapSummaryDto;
import dev.skillforge.catalogue.adapter.in.web.mapper.CatalogueWebMapper;
import dev.skillforge.catalogue.domain.model.NodeId;
import dev.skillforge.catalogue.domain.port.in.GetCertificationUseCase;
import dev.skillforge.catalogue.domain.port.in.GetNodeUseCase;
import dev.skillforge.catalogue.domain.port.in.GetRoadmapGraphUseCase;
import dev.skillforge.catalogue.domain.port.in.ListRoadmapsUseCase;
import dev.skillforge.catalogue.domain.port.in.SearchRoadmapsUseCase;
import dev.skillforge.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/** Adaptateur entrant WebFlux du contexte Catalogue (lecture). */
@RestController
@RequestMapping("/api/v1")
public class CatalogueController {

    private final ListRoadmapsUseCase listRoadmaps;
    private final SearchRoadmapsUseCase searchRoadmaps;
    private final GetRoadmapGraphUseCase getRoadmapGraph;
    private final GetNodeUseCase getNode;
    private final GetCertificationUseCase getCertification;

    public CatalogueController(ListRoadmapsUseCase listRoadmaps,
                              SearchRoadmapsUseCase searchRoadmaps,
                              GetRoadmapGraphUseCase getRoadmapGraph,
                              GetNodeUseCase getNode,
                              GetCertificationUseCase getCertification) {
        this.listRoadmaps = listRoadmaps;
        this.searchRoadmaps = searchRoadmaps;
        this.getRoadmapGraph = getRoadmapGraph;
        this.getNode = getNode;
        this.getCertification = getCertification;
    }

    /** Liste les parcours ; filtre optionnel plein texte via {@code ?q=}. */
    @GetMapping("/roadmaps")
    public Mono<ApiResponse<List<RoadmapSummaryDto>>> listRoadmaps(
            @RequestParam(name = "q", required = false) String query) {
        var source = (query == null || query.isBlank())
                ? listRoadmaps.list()
                : searchRoadmaps.search(query);
        return source.map(CatalogueWebMapper::toSummary).collectList().map(ApiResponse::ok);
    }

    /** Obtient un parcours avec son graphe (phases, nœuds, arêtes). */
    @GetMapping("/roadmaps/{slug}")
    public Mono<ApiResponse<RoadmapGraphDto>> getRoadmap(@PathVariable String slug) {
        return getRoadmapGraph.getBySlug(slug)
                .map(CatalogueWebMapper::toGraph)
                .map(ApiResponse::ok);
    }

    /** Obtient un nœud par identifiant. */
    @GetMapping("/nodes/{id}")
    public Mono<ApiResponse<NodeDto>> getNode(@PathVariable UUID id) {
        return getNode.getById(new NodeId(id))
                .map(CatalogueWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    /** Obtient la certification (et ses domaines) d'un parcours de type certification. */
    @GetMapping("/roadmaps/{slug}/certification")
    public Mono<ApiResponse<CertificationDto>> getCertification(@PathVariable String slug) {
        return getCertification.getByRoadmapSlug(slug)
                .map(CatalogueWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    /** Obtient une certification par identifiant (libellés de domaines pour les résultats d'examen). */
    @GetMapping("/certifications/{id}")
    public Mono<ApiResponse<CertificationDto>> getCertificationById(@PathVariable UUID id) {
        return getCertification.getById(id)
                .map(CatalogueWebMapper::toDto)
                .map(ApiResponse::ok);
    }
}
