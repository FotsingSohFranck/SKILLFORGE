package dev.skillforge.apprentissage.domain;

import dev.skillforge.apprentissage.domain.model.Streak;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreakTest {

    private static final LocalDate DAY = LocalDate.of(2026, 7, 18);

    @Test
    void premiere_activite_demarre_la_serie_a_1() {
        Streak s = Streak.empty().registerActivity(DAY);
        assertEquals(1, s.currentLength());
        assertEquals(1, s.longestLength());
        assertEquals(DAY, s.lastActivityDate());
    }

    @Test
    void meme_jour_ne_change_rien() {
        Streak s = Streak.empty().registerActivity(DAY).registerActivity(DAY);
        assertEquals(1, s.currentLength());
    }

    @Test
    void jour_suivant_incremente_la_serie() {
        Streak s = Streak.empty()
                .registerActivity(DAY)
                .registerActivity(DAY.plusDays(1))
                .registerActivity(DAY.plusDays(2));
        assertEquals(3, s.currentLength());
        assertEquals(3, s.longestLength());
    }

    @Test
    void un_trou_reinitialise_la_serie_mais_conserve_le_record() {
        Streak s = Streak.empty()
                .registerActivity(DAY)
                .registerActivity(DAY.plusDays(1))   // série = 2
                .registerActivity(DAY.plusDays(5));  // trou -> série = 1
        assertEquals(1, s.currentLength());
        assertEquals(2, s.longestLength(), "le plus long reste 2");
    }
}
