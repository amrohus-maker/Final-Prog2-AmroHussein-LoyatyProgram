package loyalty.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {
    @Test
    void aTransactionKeepsWhatItWasGiven() {
        Transaction earned = new Transaction(25, "  Purchase $25.00  ");
        assertEquals(25, earned.getPoints());
        assertEquals("Purchase $25.00", earned.getDescription()); // spaces are trimmed
        assertEquals(LocalDate.now().toString(), earned.getDate());

        Transaction spent = new Transaction(-60, "Redeemed: Espresso on us");
        assertEquals(-60, spent.getPoints());
    }

    @Test
    void zeroPointsOrNoDescriptionIsAMistakeInOurCode() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction(0, "nothing"));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(5, "   "));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(5, null));
    }

    @Test
    void aTransactionMadeInTheNormalWayIsValid() {
        assertTrue(new Transaction(25, "Purchase").isValid());
        assertTrue(new Transaction(-60, "Redeemed: x").isValid());
    }
}
