package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.evaluation.domain.model.Option;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.model.Question;
import dev.skillforge.evaluation.domain.model.QuestionId;
import dev.skillforge.evaluation.domain.model.QuestionType;
import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.out.QuizRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class QuizRepositoryAdapter implements QuizRepository {

    private final DatabaseClient db;

    public QuizRepositoryAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Quiz> findById(QuizId quiz) {
        UUID id = quiz.value();
        Mono<QuizRow> quizMono = db.sql("SELECT id, title, description FROM quizzes WHERE id = :id")
                .bind("id", id)
                .map((row, meta) -> new QuizRow(
                        row.get("id", UUID.class),
                        row.get("title", String.class),
                        row.get("description", String.class)))
                .one();

        var questions = db.sql("SELECT id, type, prompt, justification, position "
                        + "FROM questions WHERE quiz_id = :id ORDER BY position")
                .bind("id", id)
                .map((row, meta) -> new QuestionRow(
                        row.get("id", UUID.class),
                        row.get("type", String.class),
                        row.get("prompt", String.class),
                        row.get("justification", String.class),
                        intOr(row.get("position", Integer.class))))
                .all().collectList();

        var options = db.sql("SELECT o.id, o.question_id, o.label, o.is_correct, o.position "
                        + "FROM options o JOIN questions q ON q.id = o.question_id "
                        + "WHERE q.quiz_id = :id ORDER BY o.position")
                .bind("id", id)
                .map((row, meta) -> new OptionRow(
                        row.get("question_id", UUID.class),
                        new Option(new OptionId(row.get("id", UUID.class)),
                                row.get("label", String.class),
                                intOr(row.get("position", Integer.class)),
                                Boolean.TRUE.equals(row.get("is_correct", Boolean.class)))))
                .all().collectList();

        return quizMono.flatMap(q -> Mono.zip(questions, options).map(tuple -> assemble(q, tuple.getT1(), tuple.getT2())));
    }

    private Quiz assemble(QuizRow quiz, List<QuestionRow> questions, List<OptionRow> options) {
        Map<UUID, List<Option>> byQuestion = new LinkedHashMap<>();
        for (OptionRow o : options) {
            byQuestion.computeIfAbsent(o.questionId(), k -> new ArrayList<>()).add(o.option());
        }
        List<Question> domainQuestions = questions.stream()
                .map(q -> new Question(new QuestionId(q.id()), QuestionType.valueOf(q.type()),
                        q.prompt(), q.justification(), q.position(),
                        byQuestion.getOrDefault(q.id(), List.of())))
                .toList();
        return new Quiz(new QuizId(quiz.id()), quiz.title(), quiz.description(), domainQuestions);
    }

    private static int intOr(Integer value) {
        return value == null ? 0 : value;
    }

    private record QuizRow(UUID id, String title, String description) {
    }

    private record QuestionRow(UUID id, String type, String prompt, String justification, int position) {
    }

    private record OptionRow(UUID questionId, Option option) {
    }
}
