package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.domain.port.in.ExamImport;
import dev.skillforge.evaluation.domain.port.in.IngestExamUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/** Ingestion d'examens (pivot). Réservé INSTRUCTOR/ADMIN (règle dans la SecurityWebFilterChain). */
@RestController
@RequestMapping("/api/v1/exams")
public class ExamImportController {

    private final IngestExamUseCase ingest;

    public ExamImportController(IngestExamUseCase ingest) {
        this.ingest = ingest;
    }

    @PostMapping("/import")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> importExam(@RequestBody ExamImport pivot) {
        return ingest.ingest(pivot)
                .map(id -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.ok(Map.of("examId", id.value()))));
    }
}
