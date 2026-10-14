package dev.skillforge.contenu.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.contenu.adapter.in.web.dto.ContentImportRequest;
import dev.skillforge.contenu.adapter.in.web.dto.ContentImportResultDto;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.in.IngestNodeContentUseCase;
import dev.skillforge.contenu.domain.port.in.NodeContentImport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Ingestion du contenu d'un nœud (JSON pivot). Réservé INSTRUCTOR/ADMIN (autorisation appliquée
 * par la SecurityWebFilterChain sur {@code POST /api/v1/nodes/*​/content/import}).
 */
@RestController
@RequestMapping("/api/v1/nodes")
public class NodeContentImportController {

    private final IngestNodeContentUseCase ingest;

    public NodeContentImportController(IngestNodeContentUseCase ingest) {
        this.ingest = ingest;
    }

    @PostMapping("/{nodeId}/content/import")
    public Mono<ResponseEntity<ApiResponse<ContentImportResultDto>>> importContent(
            @PathVariable UUID nodeId, @RequestBody ContentImportRequest request) {
        NodeContentImport command = new NodeContentImport(new NodeId(nodeId), request.lessons());
        return ingest.ingest(command)
                .map(count -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.ok(new ContentImportResultDto(nodeId, count))));
    }
}
