package dev.skillforge.lab.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Tentative d'un apprenant sur une épreuve pratique.
 *
 * <p>Entité immuable : les transitions renvoient une nouvelle instance. Un statut terminal
 * (PASSED / FAILED / EXPIRED) ne peut plus changer (règle de domaine).
 */
public record PracticalAttempt(
        UUID id,
        LearnerId learner,
        PracticalTaskId taskId,
        UUID nodeId,
        AttemptStatus status,
        String output,
        Instant startedAt,
        Instant finishedAt
) {
    public PracticalAttempt {
        Objects.requireNonNull(id, "PracticalAttempt.id");
        Objects.requireNonNull(learner, "PracticalAttempt.learner");
        Objects.requireNonNull(taskId, "PracticalAttempt.taskId");
        Objects.requireNonNull(nodeId, "PracticalAttempt.nodeId");
        Objects.requireNonNull(status, "PracticalAttempt.status");
    }

    /** Crée une tentative en attente de démarrage. */
    public static PracticalAttempt pending(UUID id, LearnerId learner, PracticalTaskId taskId,
                                           UUID nodeId, Instant startedAt) {
        return new PracticalAttempt(id, learner, taskId, nodeId, AttemptStatus.PENDING, null, startedAt, null);
    }

    public boolean passed() {
        return status == AttemptStatus.PASSED;
    }

    /** Marque l'environnement comme démarré. */
    public PracticalAttempt running() {
        ensureNotTerminal();
        return new PracticalAttempt(id, learner, taskId, nodeId, AttemptStatus.RUNNING, output, startedAt, finishedAt);
    }

    /** Verdict : réussite. */
    public PracticalAttempt markPassed(String output, Instant finishedAt) {
        return finish(AttemptStatus.PASSED, output, finishedAt);
    }

    /** Verdict : échec. */
    public PracticalAttempt markFailed(String output, Instant finishedAt) {
        return finish(AttemptStatus.FAILED, output, finishedAt);
    }

    /** Session expirée (TTL dépassé). */
    public PracticalAttempt markExpired(Instant finishedAt) {
        return finish(AttemptStatus.EXPIRED, output, finishedAt);
    }

    private PracticalAttempt finish(AttemptStatus terminal, String output, Instant finishedAt) {
        ensureNotTerminal();
        return new PracticalAttempt(id, learner, taskId, nodeId, terminal, output, startedAt, finishedAt);
    }

    private void ensureNotTerminal() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Tentative deja terminee (" + status + "), transition interdite");
        }
    }
}
