package dev.skillforge.catalogue.domain.port.in;

import java.util.List;

/**
 * Commande d'import d'une roadmap complète (image du JSON pivot, cf. docs/PIVOT-ROADMAP.md).
 * Modèle d'entrée neutre : le domaine ne dépend ni du web ni de la persistance.
 */
public record RoadmapImport(
        String slug,
        String title,
        String summary,
        String kind,
        Integer position,
        Certification certification,
        List<Phase> phases,
        List<Node> nodes,
        List<Edge> edges) {

    public record Phase(String title, Integer position) {
    }

    public record Node(
            String slug,
            String title,
            String summary,
            String phase,
            String importance,
            Integer estimatedMinutes,
            Integer position,
            List<NodeExamDomain> examDomains) {
    }

    public record Edge(String source, String target, String kind) {
    }

    public record Certification(
            String code,
            String examFormat,
            Integer durationMinutes,
            Double passingScore,
            List<ExamDomain> domains) {
    }

    public record ExamDomain(String name, Double weight, Integer position) {
    }

    public record NodeExamDomain(String domain, Double weight) {
    }
}
