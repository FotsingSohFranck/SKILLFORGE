package forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests caches de l'epreuve « regrouper par parite ». Non modifiables par l'apprenant. */
class SolutionTest {

    @Test
    @DisplayName("regroupe pairs et impairs en preservant l'ordre")
    void groupsByParityInOrder() {
        Map<String, List<Integer>> result = new Solution().groupByParity(List.of(1, 2, 3, 4, 5, 6));
        assertEquals(List.of(2, 4, 6), result.get("pair"));
        assertEquals(List.of(1, 3, 5), result.get("impair"));
    }

    @Test
    @DisplayName("gere les nombres negatifs et le zero")
    void handlesNegativesAndZero() {
        Map<String, List<Integer>> result = new Solution().groupByParity(List.of(0, -2, -3, 7));
        assertEquals(List.of(0, -2), result.get("pair"));
        assertEquals(List.of(-3, 7), result.get("impair"));
    }

    @Test
    @DisplayName("liste vide -> groupes vides (jamais null)")
    void emptyInputYieldsEmptyGroups() {
        Map<String, List<Integer>> result = new Solution().groupByParity(List.of());
        assertTrue(result.getOrDefault("pair", List.of()).isEmpty(), "le groupe pair doit etre vide");
        assertTrue(result.getOrDefault("impair", List.of()).isEmpty(), "le groupe impair doit etre vide");
    }
}
