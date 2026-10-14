package dev.skillforge.contenu.adapter.in.web.dto;

import dev.skillforge.contenu.domain.port.in.NodeContentImport;

import java.util.List;

/** Corps de requête d'import de contenu (le nodeId provient de l'URL). */
public record ContentImportRequest(
        List<NodeContentImport.LessonImport> lessons) {
}
