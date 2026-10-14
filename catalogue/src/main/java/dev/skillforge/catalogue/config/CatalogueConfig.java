package dev.skillforge.catalogue.config;

import dev.skillforge.catalogue.application.GetCertificationService;
import dev.skillforge.catalogue.application.GetNodeService;
import dev.skillforge.catalogue.application.GetRoadmapGraphService;
import dev.skillforge.catalogue.application.IngestRoadmapService;
import dev.skillforge.catalogue.application.ListRoadmapsService;
import dev.skillforge.catalogue.application.SearchRoadmapsService;
import dev.skillforge.catalogue.domain.port.in.GetCertificationUseCase;
import dev.skillforge.catalogue.domain.port.in.GetNodeUseCase;
import dev.skillforge.catalogue.domain.port.in.GetRoadmapGraphUseCase;
import dev.skillforge.catalogue.domain.port.in.IngestRoadmapUseCase;
import dev.skillforge.catalogue.domain.port.in.ListRoadmapsUseCase;
import dev.skillforge.catalogue.domain.port.in.SearchRoadmapsUseCase;
import dev.skillforge.catalogue.domain.port.out.CertificationRepository;
import dev.skillforge.catalogue.domain.port.out.EdgeRepository;
import dev.skillforge.catalogue.domain.port.out.NodeRepository;
import dev.skillforge.catalogue.domain.port.out.PhaseRepository;
import dev.skillforge.catalogue.domain.port.out.RoadmapImporter;
import dev.skillforge.catalogue.domain.port.out.RoadmapRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage du contexte Catalogue : les services applicatifs (purs) sont exposés comme beans,
 * en leur injectant les adaptateurs de persistance (implémentations des ports sortants).
 */
@Configuration
public class CatalogueConfig {

    @Bean
    ListRoadmapsUseCase listRoadmapsUseCase(RoadmapRepository roadmaps) {
        return new ListRoadmapsService(roadmaps);
    }

    @Bean
    SearchRoadmapsUseCase searchRoadmapsUseCase(RoadmapRepository roadmaps) {
        return new SearchRoadmapsService(roadmaps);
    }

    @Bean
    GetRoadmapGraphUseCase getRoadmapGraphUseCase(RoadmapRepository roadmaps, PhaseRepository phases,
                                                  NodeRepository nodes, EdgeRepository edges) {
        return new GetRoadmapGraphService(roadmaps, phases, nodes, edges);
    }

    @Bean
    GetNodeUseCase getNodeUseCase(NodeRepository nodes) {
        return new GetNodeService(nodes);
    }

    @Bean
    GetCertificationUseCase getCertificationUseCase(CertificationRepository certifications) {
        return new GetCertificationService(certifications);
    }

    @Bean
    IngestRoadmapUseCase ingestRoadmapUseCase(RoadmapImporter importer) {
        return new IngestRoadmapService(importer);
    }
}
