package dev.skillforge.contenu.adapter.out.persistence;

import dev.skillforge.contenu.domain.port.in.NodeContentImport;
import dev.skillforge.contenu.domain.port.out.NodeContentImporter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Adaptateur R2DBC : persistance idempotente du contenu d'un nœud (transactionnel).
 * Les leçons sont upsertées par {@code (node_id, slug)} (id préservé) ; les sections d'une
 * leçon sont intégralement remplacées à chaque import.
 */
@Component
public class NodeContentImporterAdapter implements NodeContentImporter {

    private final DatabaseClient db;

    public NodeContentImporterAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    @Transactional
    public Mono<Integer> importContent(NodeContentImport command) {
        UUID nodeId = command.nodeId().value();
        return Flux.fromIterable(safe(command.lessons()))
                .concatMap(lesson -> upsertLesson(nodeId, lesson)
                        .flatMap(lessonId -> replaceSections(lessonId, lesson).thenReturn(lessonId)))
                .count()
                .map(Long::intValue);
    }

    private Mono<UUID> upsertLesson(UUID nodeId, NodeContentImport.LessonImport lesson) {
        return db.sql("""
                        INSERT INTO lessons(node_id, slug, title, position)
                        VALUES (:nid, :slug, :title, :position)
                        ON CONFLICT (node_id, slug) DO UPDATE SET
                            title = EXCLUDED.title, position = EXCLUDED.position
                        RETURNING id
                        """)
                .bind("nid", nodeId)
                .bind("slug", lesson.slug())
                .bind("title", lesson.title())
                .bind("position", lesson.position() == null ? 0 : lesson.position())
                .map((row, meta) -> row.get("id", UUID.class)).one();
    }

    private Mono<Void> replaceSections(UUID lessonId, NodeContentImport.LessonImport lesson) {
        return db.sql("DELETE FROM sections WHERE lesson_id = :lid")
                .bind("lid", lessonId).fetch().rowsUpdated()
                .thenMany(Flux.fromIterable(safe(lesson.sections())))
                .concatMap(section -> insertSection(lessonId, section))
                .then();
    }

    private Mono<Long> insertSection(UUID lessonId, NodeContentImport.SectionImport s) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO sections(lesson_id, type, position, title, body,
                                             code_language, diagram_format, solution, quiz_id)
                        VALUES (:lid, :type, :position, :title, :body, :lang, :diagram, :solution, :quiz)
                        """)
                .bind("lid", lessonId)
                .bind("type", s.type().trim().toUpperCase())
                .bind("position", s.position() == null ? 0 : s.position());
        spec = bindNullable(spec, "title", s.title(), String.class);
        spec = bindNullable(spec, "body", s.body(), String.class);
        spec = bindNullable(spec, "lang", s.codeLanguage(), String.class);
        spec = bindNullable(spec, "diagram",
                s.diagramFormat() == null ? null : s.diagramFormat().trim().toUpperCase(), String.class);
        spec = bindNullable(spec, "solution", s.solution(), String.class);
        spec = bindNullable(spec, "quiz", s.quizId(), UUID.class);
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
