package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.port.in.ExamImport;
import dev.skillforge.evaluation.domain.port.out.ExamImporter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/** Adaptateur R2DBC : persistance idempotente d'un examen (upsert par id ; items remplacés). */
@Component
public class ExamImporterAdapter implements ExamImporter {

    private final DatabaseClient db;

    public ExamImporterAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    @Transactional
    public Mono<ExamId> importExam(ExamImport c) {
        UUID examId = c.id();
        return upsertExam(c)
                .then(db.sql("DELETE FROM exam_questions WHERE mock_exam_id = :id")
                        .bind("id", examId).fetch().rowsUpdated())
                .thenMany(Flux.fromIterable(safe(c.items())))
                .concatMap(item -> insertItem(examId, item))
                .then(Mono.just(new ExamId(examId)));
    }

    private Mono<Long> upsertExam(ExamImport c) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO mock_exams(id, certification_id, title, format, duration_minutes, passing_score)
                        VALUES (:id, :cert, :title, :format, :duration, :passing)
                        ON CONFLICT (id) DO UPDATE SET
                            certification_id = EXCLUDED.certification_id, title = EXCLUDED.title,
                            format = EXCLUDED.format, duration_minutes = EXCLUDED.duration_minutes,
                            passing_score = EXCLUDED.passing_score
                        """)
                .bind("id", c.id())
                .bind("cert", c.certificationId())
                .bind("title", c.title())
                .bind("format", c.format() == null ? "MCQ" : c.format().trim().toUpperCase())
                .bind("duration", c.durationMinutes());
        spec = c.passingScore() != null
                ? spec.bind("passing", c.passingScore())
                : spec.bindNull("passing", Double.class);
        return spec.fetch().rowsUpdated();
    }

    private Mono<Long> insertItem(UUID examId, ExamImport.ItemImport item) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO exam_questions(mock_exam_id, exam_domain_id, kind, question_id,
                                                   prompt, task_solution, points, position)
                        VALUES (:exam, :domain, :kind, :question, :prompt, :solution, :points, :position)
                        """)
                .bind("exam", examId)
                .bind("domain", item.examDomainId())
                .bind("kind", item.kind().trim().toUpperCase())
                .bind("points", item.points() == null ? 1.0 : item.points())
                .bind("position", item.position() == null ? 0 : item.position());
        spec = bindNullable(spec, "question", item.questionId(), UUID.class);
        spec = bindNullable(spec, "prompt", item.prompt(), String.class);
        spec = bindNullable(spec, "solution", item.taskSolution(), String.class);
        return spec.fetch().rowsUpdated();
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec, String name, Object value, Class<?> type) {
        return value != null ? spec.bind(name, value) : spec.bindNull(name, type);
    }
}
