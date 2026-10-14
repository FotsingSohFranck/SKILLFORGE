package dev.skillforge.contenu.adapter.out.persistence;

import dev.skillforge.contenu.domain.model.DiagramFormat;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.model.SectionId;
import dev.skillforge.contenu.domain.model.SectionType;
import dev.skillforge.contenu.domain.port.in.SectionContent;
import dev.skillforge.contenu.domain.port.out.SectionWriteStore;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class SectionWriteStoreAdapter implements SectionWriteStore {

    private static final String RETURNING =
            " RETURNING id, type, position, title, body, code_language, diagram_format, solution, quiz_id";

    private final DatabaseClient db;

    public SectionWriteStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Section> insert(LessonId lesson, SectionContent c) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO sections(lesson_id, type, position, title, body,
                                             code_language, diagram_format, solution, quiz_id)
                        VALUES (:lid, :type, :position, :title, :body, :lang, :diagram, :solution, :quiz)
                        """ + RETURNING)
                .bind("lid", lesson.value())
                .bind("type", c.type().trim().toUpperCase())
                .bind("position", c.position() == null ? 0 : c.position());
        return bindContent(spec, c).map(SectionWriteStoreAdapter::map).one();
    }

    @Override
    public Mono<Section> update(SectionId section, SectionContent c) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        UPDATE sections SET type = :type, position = :position, title = :title,
                            body = :body, code_language = :lang, diagram_format = :diagram,
                            solution = :solution, quiz_id = :quiz
                        WHERE id = :id
                        """ + RETURNING)
                .bind("id", section.value())
                .bind("type", c.type().trim().toUpperCase())
                .bind("position", c.position() == null ? 0 : c.position());
        return bindContent(spec, c).map(SectionWriteStoreAdapter::map).one();
    }

    @Override
    public Mono<Long> delete(SectionId section) {
        return db.sql("DELETE FROM sections WHERE id = :id")
                .bind("id", section.value()).fetch().rowsUpdated();
    }

    @Override
    public Mono<Void> updatePosition(SectionId section, int position) {
        return db.sql("UPDATE sections SET position = :position WHERE id = :id")
                .bind("position", position).bind("id", section.value())
                .fetch().rowsUpdated().then();
    }

    private static DatabaseClient.GenericExecuteSpec bindContent(
            DatabaseClient.GenericExecuteSpec spec, SectionContent c) {
        spec = bindNullable(spec, "title", c.title(), String.class);
        spec = bindNullable(spec, "body", c.body(), String.class);
        spec = bindNullable(spec, "lang", c.codeLanguage(), String.class);
        spec = bindNullable(spec, "diagram",
                c.diagramFormat() == null ? null : c.diagramFormat().trim().toUpperCase(), String.class);
        spec = bindNullable(spec, "solution", c.solution(), String.class);
        spec = bindNullable(spec, "quiz", c.quizId(), UUID.class);
        return spec;
    }

    private static DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec, String name, Object value, Class<?> type) {
        return value != null ? spec.bind(name, value) : spec.bindNull(name, type);
    }

    private static Section map(Readable row) {
        Integer position = row.get("position", Integer.class);
        String diagram = row.get("diagram_format", String.class);
        return new Section(
                new SectionId(row.get("id", UUID.class)),
                SectionType.valueOf(row.get("type", String.class)),
                position == null ? 0 : position,
                row.get("title", String.class),
                row.get("body", String.class),
                row.get("code_language", String.class),
                diagram == null ? null : DiagramFormat.valueOf(diagram),
                row.get("solution", String.class),
                row.get("quiz_id", UUID.class));
    }
}
