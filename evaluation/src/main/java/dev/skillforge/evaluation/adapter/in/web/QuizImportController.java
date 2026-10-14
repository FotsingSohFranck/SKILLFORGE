package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.domain.port.in.IngestQuizUseCase;
import dev.skillforge.evaluation.domain.port.in.QuizImport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/** Ingestion de quiz (pivot). Réservé INSTRUCTOR/ADMIN (règle dans la SecurityWebFilterChain). */
@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizImportController {

    private final IngestQuizUseCase ingest;

    public QuizImportController(IngestQuizUseCase ingest) {
        this.ingest = ingest;
    }

    @PostMapping("/import")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> importQuiz(@RequestBody QuizImport pivot) {
        return ingest.ingest(pivot)
                .map(id -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.ok(Map.of("quizId", id.value()))));
    }
}
