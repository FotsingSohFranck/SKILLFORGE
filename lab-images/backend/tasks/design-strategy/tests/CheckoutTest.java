package forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests caches de l'epreuve « regles de remise ». Non modifiables par l'apprenant. */
class CheckoutTest {

    @Test
    @DisplayName("REGULAR : aucune remise")
    void regularHasNoDiscount() {
        assertEquals(100, new Checkout().finalPrice(100, "REGULAR"));
    }

    @Test
    @DisplayName("VIP : 20% de remise, arrondi a l'entier inferieur")
    void vipGetsTwentyPercent() {
        assertEquals(80, new Checkout().finalPrice(100, "VIP"));
        assertEquals(79, new Checkout().finalPrice(99, "VIP")); // 79.2 -> 79
    }

    @Test
    @DisplayName("EMPLOYEE : 30% de remise")
    void employeeGetsThirtyPercent() {
        assertEquals(70, new Checkout().finalPrice(100, "EMPLOYEE"));
    }

    @Test
    @DisplayName("type inconnu : IllegalArgumentException")
    void unknownTypeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Checkout().finalPrice(100, "GOLD"));
    }
}
