package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.QuizImport;
import dev.skillforge.evaluation.domain.port.out.QuizImporter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Adaptateur R2DBC : persistance idempotente d'un quiz (transactionnel).
 * Le quiz est upserté par {@code id} (préservé, cohérent avec la section de contenu) ; ses
 * questions et options sont intégralement remplacées à chaque import.
 */
@Component
public class QuizImporterAdapter implements QuizImporter {

    private final DatabaseClient db;

    public QuizImporterAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    @Transactional
    public Mono<QuizId> importQuiz(QuizImport c) {
        UUID quizId = c.id();
        return upsertQuiz(c)
                .then(db.sql("DELETE FROM questions WHERE quiz_id = :id").bind("id", quizId).fetch().rowsUpdated())
                .thenMany(Flux.fromIterable(safe(c.questions())))
                .concatMap(question -> insertQuestion(quizId, question))
                .then(Mono.just(new QuizId(quizId)));
    }

    private Mono<Long> upsertQuiz(QuizImport c) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO quizzes(id, node_id, lesson_id, title, description)
                        VALUES (:id, :node, :lesson, :title, :description)
                        ON CONFLICT (id) DO UPDATE SET
                            node_id = EXCLUDED.node_id, lesson_id = EXCLUDED.lesson_id,
                            title = EXCLUDED.title, description = EXCLUDED.description
                        """)
                .bind("id", c.id()).bind("title", c.title());
        spec = bindNullable(spec, "node", c.nodeId(), UUID.class);
        spec = bindNullable(spec, "lesson", c.lessonId(), UUID.class);
        spec = bindNullable(spec, "description", c.description(), String.class);
        return spec.fetch().rowsUpdated();
    }

    private Mono<Void> insertQuestion(UUID quizId, QuizImport.QuestionImport q) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO questions(quiz_id, type, prompt, justification, position)
                        VALUES (:qz, :type, :prompt, :justif, :position) RETURNING id
                        """)
                .bind("qz", quizId)
                .bind("type", q.type().trim().toUpperCase())
                .bind("prompt", q.prompt())
                .bind("position", q.position() == null ? 0 : q.position());
        spec = bindNullable(spec, "justif", q.justification(), String.class);
        return spec.map((row, meta) -> row.get("id", UUID.class)).one()
                .flatMapMany(questionId -> Flux.fromIterable(safe(q.options()))
                        .concatMap(option -> insertOption(questionId, option)))
                .then();
    }

    private Mono<Long> insertOption(UUID questionId, QuizImport.OptionImport o) {
        return db.sql("INSERT INTO options(question_id, label, is_correct, position) "
                        + "VALUES (:q, :label, :correct, :position)")
                .bind("q", questionId)
                .bind("label", o.label())
                .bind("correct", Boolean.TRUE.equals(o.correct()))
                .bind("position", o.position() == null ? 0 : o.position())
                .fetch().rowsUpdated();
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec, String name, Object value, Class<?> type) {
        return value != null ? spec.bind(name, value) : spec.bindNull(name, type);
    }
}
