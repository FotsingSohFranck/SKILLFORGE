package dev.skillforge.catalogue.adapter.in.web.mapper;

import dev.skillforge.catalogue.adapter.in.web.dto.CertificationDto;
import dev.skillforge.catalogue.adapter.in.web.dto.EdgeDto;
import dev.skillforge.catalogue.adapter.in.web.dto.NodeDto;
import dev.skillforge.catalogue.adapter.in.web.dto.PhaseDto;
import dev.skillforge.catalogue.adapter.in.web.dto.RoadmapGraphDto;
import dev.skillforge.catalogue.adapter.in.web.dto.RoadmapSummaryDto;
import dev.skillforge.catalogue.domain.model.Certification;
import dev.skillforge.catalogue.domain.model.Edge;
import dev.skillforge.catalogue.domain.model.Node;
import dev.skillforge.catalogue.domain.model.Phase;
import dev.skillforge.catalogue.domain.model.Roadmap;
import dev.skillforge.catalogue.domain.model.RoadmapGraph;

/** Traduction modèle de domaine → DTO web (hors du domaine). */
public final class CatalogueWebMapper {

    private CatalogueWebMapper() {
    }

    public static RoadmapSummaryDto toSummary(Roadmap r) {
        return new RoadmapSummaryDto(
                r.id().value(), r.slug(), r.title(), r.summary(), r.kind().name(), r.position());
    }

    public static PhaseDto toDto(Phase p) {
        return new PhaseDto(p.id().value(), p.title(), p.position());
    }

    public static NodeDto toDto(Node n) {
        return new NodeDto(
                n.id().value(),
                n.roadmapId().value(),
                n.phaseId() == null ? null : n.phaseId().value(),
                n.slug(), n.title(), n.summary(),
                n.importance().name(), n.estimatedMinutes(), n.position());
    }

    public static EdgeDto toDto(Edge e) {
        return new EdgeDto(
                e.id().value(), e.sourceNodeId().value(), e.targetNodeId().value(), e.kind().name());
    }

    public static CertificationDto toDto(Certification c) {
        return new CertificationDto(
                c.id(), c.code(), c.examFormat(), c.durationMinutes(), c.passingScore(),
                c.domains().stream()
                        .map(d -> new CertificationDto.ExamDomainDto(d.id(), d.name(), d.weight(), d.position()))
                        .toList());
    }

    public static RoadmapGraphDto toGraph(RoadmapGraph g) {
        return new RoadmapGraphDto(
                toSummary(g.roadmap()),
                g.phases().stream().map(CatalogueWebMapper::toDto).toList(),
                g.nodes().stream().map(CatalogueWebMapper::toDto).toList(),
                g.edges().stream().map(CatalogueWebMapper::toDto).toList());
    }
}
