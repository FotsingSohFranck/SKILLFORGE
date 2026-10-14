package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.evaluation.domain.model.AnswerResult;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.out.AttemptStore;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class AttemptStoreAdapter implements AttemptStore {

    private final DatabaseClient db;

    public AttemptStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    @Transactional
    public Mono<Void> save(LearnerId learner, QuizId quiz, int score, int maxScore, List<AnswerResult> answers) {
        return db.sql("INSERT INTO attempts(user_id, quiz_id, score, max_score, submitted_at) "
                        + "VALUES (:uid, :qid, :score, :max, now()) RETURNING id")
                .bind("uid", learner.value()).bind("qid", quiz.value())
                .bind("score", score).bind("max", maxScore)
                .map((row, meta) -> row.get("id", UUID.class)).one()
                .flatMap(attemptId -> Flux.fromIterable(answers)
                        .concatMap(answer -> insertAnswer(attemptId, answer))
                        .then());
    }

    private Mono<Long> insertAnswer(UUID attemptId, AnswerResult answer) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO attempt_answers(attempt_id, question_id, selected_option_id, is_correct)
                        VALUES (:aid, :qid, :oid, :correct)
                        """)
                .bind("aid", attemptId)
                .bind("qid", answer.questionId().value())
                .bind("correct", answer.correct());
        spec = answer.selectedOptionId() != null
                ? spec.bind("oid", answer.selectedOptionId().value())
                : spec.bindNull("oid", UUID.class);
        return spec.fetch().rowsUpdated();
    }
}
