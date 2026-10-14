package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.EdgeKind;
import dev.skillforge.catalogue.domain.model.NodeImportance;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.model.RoadmapKind;
import dev.skillforge.catalogue.domain.port.in.IngestRoadmapUseCase;
import dev.skillforge.catalogue.domain.port.in.RoadmapImport;
import dev.skillforge.catalogue.domain.port.out.RoadmapImporter;
import dev.skillforge.common.error.BusinessRuleException;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Valide le pivot d'import puis délègue la persistance idempotente à {@link RoadmapImporter}.
 * La validation lève une {@link BusinessRuleException} (code {@code INVALID_IMPORT}) → HTTP 422.
 */
public class IngestRoadmapService implements IngestRoadmapUseCase {

    private final RoadmapImporter importer;

    public IngestRoadmapService(RoadmapImporter importer) {
        this.importer = importer;
    }

    @Override
    public Mono<RoadmapId> ingest(RoadmapImport command) {
        try {
            validate(command);
        } catch (BusinessRuleException ex) {
            return Mono.error(ex);
        }
        return importer.importRoadmap(command);
    }

    private void validate(RoadmapImport c) {
        require(c != null, "Le document d'import est requis.");
        require(notBlank(c.slug()), "Le slug de la roadmap est requis.");
        require(notBlank(c.title()), "Le titre de la roadmap est requis.");

        RoadmapKind kind = parseEnum(RoadmapKind.class, c.kind() == null ? "TRACK" : c.kind(),
                "kind", "TRACK/CERTIFICATION");
        if (kind == RoadmapKind.CERTIFICATION) {
            require(c.certification() != null,
                    "Le bloc certification est requis pour une roadmap de type CERTIFICATION.");
            require(notBlank(c.certification().code()), "Le code de certification est requis.");
        }

        Set<String> phaseTitles = new HashSet<>();
        for (RoadmapImport.Phase phase : safe(c.phases())) {
            require(notBlank(phase.title()), "Chaque phase doit avoir un titre.");
            require(phaseTitles.add(phase.title()), "Titre de phase en double : " + phase.title());
        }

        Set<String> nodeSlugs = new HashSet<>();
        for (RoadmapImport.Node node : safe(c.nodes())) {
            require(notBlank(node.slug()), "Chaque nœud doit avoir un slug.");
            require(notBlank(node.title()), "Chaque nœud doit avoir un titre.");
            require(nodeSlugs.add(node.slug()), "Slug de nœud en double : " + node.slug());
            if (node.importance() != null) {
                parseEnum(NodeImportance.class, node.importance(), "importance",
                        "CORE/RECOMMENDED/OPTIONAL");
            }
            if (node.phase() != null && !phaseTitles.contains(node.phase())) {
                fail("Le nœud '" + node.slug() + "' référence une phase inconnue : " + node.phase());
            }
        }

        for (RoadmapImport.Edge edge : safe(c.edges())) {
            require(notBlank(edge.source()) && notBlank(edge.target()),
                    "Chaque arête doit avoir une source et une cible.");
            require(nodeSlugs.contains(edge.source()),
                    "Arête : nœud source inconnu : " + edge.source());
            require(nodeSlugs.contains(edge.target()),
                    "Arête : nœud cible inconnu : " + edge.target());
            parseEnum(EdgeKind.class, edge.kind() == null ? "PREREQ" : edge.kind(), "kind", "PREREQ");
        }
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    private static void fail(String message) {
        throw new BusinessRuleException("INVALID_IMPORT", message);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field, String allowed) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessRuleException("INVALID_IMPORT",
                    "Valeur invalide pour " + field + " : '" + value + "' (attendu : " + allowed + ").");
        }
    }
}
