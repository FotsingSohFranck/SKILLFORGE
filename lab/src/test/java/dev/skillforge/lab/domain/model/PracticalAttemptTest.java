package dev.skillforge.lab.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticalAttemptTest {

    private PracticalAttempt pending() {
        return PracticalAttempt.pending(
                UUID.randomUUID(),
                LearnerId.of(UUID.randomUUID()),
                PracticalTaskId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void passe_de_pending_a_passed_avec_verdict() {
        PracticalAttempt done = pending().running().markPassed("PRATIQUE_OK", Instant.parse("2026-01-01T00:05:00Z"));
        assertEquals(AttemptStatus.PASSED, done.status());
        assertTrue(done.passed());
        assertEquals("PRATIQUE_OK", done.output());
        assertEquals(Instant.parse("2026-01-01T00:05:00Z"), done.finishedAt());
    }

    @Test
    void echec_marque_failed_non_passed() {
        PracticalAttempt done = pending().markFailed("tests rouges", Instant.parse("2026-01-01T00:05:00Z"));
        assertEquals(AttemptStatus.FAILED, done.status());
        assertFalse(done.passed());
    }

    @Test
    void un_statut_terminal_ne_peut_plus_changer() {
        PracticalAttempt passed = pending().markPassed("ok", Instant.parse("2026-01-01T00:05:00Z"));
        assertTrue(passed.status().isTerminal());
        assertThrows(IllegalStateException.class, () -> passed.markFailed("x", Instant.now()));
        assertThrows(IllegalStateException.class, passed::running);
    }

    @Test
    void la_tache_exige_un_timeout_strictement_positif() {
        assertThrows(IllegalArgumentException.class, () -> new PracticalTask(
                PracticalTaskId.of(UUID.randomUUID()), UUID.randomUUID(),
                "Titre", "eclipse-temurin:21-jdk", "Fais la chose", null, "grader/check.sh", 0, null));
    }

    @Test
    void la_tache_refuse_un_champ_texte_vide() {
        assertThrows(IllegalArgumentException.class, () -> new PracticalTask(
                PracticalTaskId.of(UUID.randomUUID()), UUID.randomUUID(),
                " ", "image", "instructions", null, "ref", 900, null));
    }
}
