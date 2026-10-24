package forge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Test jouet : force le telechargement de JUnit + surefire dans le depot Maven de l'image. */
class PreheatTest {

    @Test
    void answerIs42() {
        assertEquals(42, Preheat.answer());
    }
}
