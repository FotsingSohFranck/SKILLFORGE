package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.evaluation.domain.model.ExamDomainId;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.ExamItem;
import dev.skillforge.evaluation.domain.model.ExamItemId;
import dev.skillforge.evaluation.domain.model.ExamItemKind;
import dev.skillforge.evaluation.domain.model.ExamSummary;
import dev.skillforge.evaluation.domain.model.MockExam;
import dev.skillforge.evaluation.domain.model.Option;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.port.out.ExamRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ExamRepositoryAdapter implements ExamRepository {

    private final DatabaseClient db;

    public ExamRepositoryAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<MockExam> findById(ExamId exam) {
        UUID id = exam.value();
        Mono<ExamRow> examMono = db.sql("SELECT id, certification_id, title, format, "
                        + "duration_minutes, passing_score FROM mock_exams WHERE id = :id")
                .bind("id", id)
                .map((row, meta) -> new ExamRow(
                        row.get("id", UUID.class),
                        row.get("certification_id", UUID.class),
                        row.get("title", String.class),
                        row.get("format", String.class),
                        intOr(row.get("duration_minutes", Integer.class)),
                        toDoubleOrNull(row.get("passing_score", BigDecimal.class))))
                .one();

        var items = db.sql("SELECT id, exam_domain_id, kind, prompt, task_solution, points, position "
                        + "FROM exam_questions WHERE mock_exam_id = :id ORDER BY position")
                .bind("id", id)
                .map((row, meta) -> new ItemRow(
                        row.get("id", UUID.class),
                        row.get("exam_domain_id", UUID.class),
                        row.get("kind", String.class),
                        row.get("prompt", String.class),
                        row.get("task_solution", String.class),
                        toDouble(row.get("points", BigDecimal.class)),
                        intOr(row.get("position", Integer.class))))
                .all().collectList();

        var options = db.sql("SELECT eq.id AS item_id, o.id, o.label, o.is_correct, o.position "
                        + "FROM exam_questions eq JOIN options o ON o.question_id = eq.question_id "
                        + "WHERE eq.mock_exam_id = :id AND eq.kind = 'MCQ' ORDER BY o.position")
                .bind("id", id)
                .map((row, meta) -> new OptionRow(
                        row.get("item_id", UUID.class),
                        new Option(new OptionId(row.get("id", UUID.class)),
                                row.get("label", String.class),
                                intOr(row.get("position", Integer.class)),
                                Boolean.TRUE.equals(row.get("is_correct", Boolean.class)))))
                .all().collectList();

        return examMono.flatMap(e -> Mono.zip(items, options)
                .map(tuple -> assemble(e, tuple.getT1(), tuple.getT2())));
    }

    @Override
    public Flux<ExamSummary> findByCertification(UUID certificationId) {
        return db.sql("SELECT id, title, format, duration_minutes FROM mock_exams "
                        + "WHERE certification_id = :cid ORDER BY title")
                .bind("cid", certificationId)
                .map((row, meta) -> new ExamSummary(
                        new ExamId(row.get("id", UUID.class)),
                        row.get("title", String.class),
                        row.get("format", String.class),
                        intOr(row.get("duration_minutes", Integer.class))))
                .all();
    }

    private MockExam assemble(ExamRow exam, List<ItemRow> items, List<OptionRow> options) {
        Map<UUID, List<Option>> byItem = new LinkedHashMap<>();
        for (OptionRow o : options) {
            byItem.computeIfAbsent(o.itemId(), k -> new ArrayList<>()).add(o.option());
        }
        List<ExamItem> domainItems = items.stream()
                .map(i -> new ExamItem(
                        new ExamItemId(i.id()),
                        new ExamDomainId(i.domainId()),
                        ExamItemKind.valueOf(i.kind()),
                        i.prompt(),
                        i.points(),
                        i.position(),
                        byItem.getOrDefault(i.id(), List.of()),
                        i.taskSolution()))
                .toList();
        return new MockExam(new ExamId(exam.id()), exam.certificationId(), exam.title(),
                exam.format(), exam.durationMinutes(), exam.passingScore(), domainItems);
    }

    private static int intOr(Integer v) {
        return v == null ? 0 : v;
    }

    private static double toDouble(BigDecimal v) {
        return v == null ? 0.0 : v.doubleValue();
    }

    private static Double toDoubleOrNull(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }

    private record ExamRow(UUID id, UUID certificationId, String title, String format,
                           int durationMinutes, Double passingScore) {
    }

    private record ItemRow(UUID id, UUID domainId, String kind, String prompt, String taskSolution,
                           double points, int position) {
    }

    private record OptionRow(UUID itemId, Option option) {
    }
}
