package dev.skillforge.catalogue.adapter.out.persistence.mapper;

import dev.skillforge.catalogue.adapter.out.persistence.entity.EdgeEntity;
import dev.skillforge.catalogue.adapter.out.persistence.entity.NodeEntity;
import dev.skillforge.catalogue.adapter.out.persistence.entity.PhaseEntity;
import dev.skillforge.catalogue.adapter.out.persistence.entity.RoadmapEntity;
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

/** Traduction entité de persistance → modèle de domaine (hors du domaine). */
public final class CataloguePersistenceMapper {

    private CataloguePersistenceMapper() {
    }

    public static Roadmap toDomain(RoadmapEntity e) {
        return new Roadmap(
                new RoadmapId(e.id()),
                e.slug(),
                e.title(),
                e.summary(),
                RoadmapKind.valueOf(e.kind()),
                e.position() == null ? 0 : e.position());
    }

    public static Phase toDomain(PhaseEntity e) {
        return new Phase(
                new PhaseId(e.id()),
                e.title(),
                e.position() == null ? 0 : e.position());
    }

    public static Node toDomain(NodeEntity e) {
        return new Node(
                new NodeId(e.id()),
                new RoadmapId(e.roadmapId()),
                e.phaseId() == null ? null : new PhaseId(e.phaseId()),
                e.slug(),
                e.title(),
                e.summary(),
                NodeImportance.valueOf(e.importance()),
                e.estimatedMinutes(),
                e.position() == null ? 0 : e.position());
    }

    public static Edge toDomain(EdgeEntity e) {
        return new Edge(
                new EdgeId(e.id()),
                new NodeId(e.sourceNodeId()),
                new NodeId(e.targetNodeId()),
                EdgeKind.valueOf(e.kind()));
    }
}
