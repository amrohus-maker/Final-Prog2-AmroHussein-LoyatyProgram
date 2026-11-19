package loyalty.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TierTest {
    @Test
    void tiersStartAtTheRightTotalPoints() {
        assertEquals(Tier.BRONZE, Tier.forTotalPoints(0));
        assertEquals(Tier.BRONZE, Tier.forTotalPoints(299));
        assertEquals(Tier.SILVER, Tier.forTotalPoints(300));
        assertEquals(Tier.SILVER, Tier.forTotalPoints(999));
        assertEquals(Tier.GOLD, Tier.forTotalPoints(1000));
        assertEquals(Tier.GOLD, Tier.forTotalPoints(50000));
    }

    @Test
    void higherTiersGetBiggerBonuses() {
        assertEquals(0, Tier.BRONZE.getBonusPercent());
        assertEquals(10, Tier.SILVER.getBonusPercent());
        assertEquals(25, Tier.GOLD.getBonusPercent());
    }

    @Test
    void isAtLeastComparesTheLevels() {
        assertTrue(Tier.GOLD.isAtLeast(Tier.SILVER));
        assertTrue(Tier.SILVER.isAtLeast(Tier.SILVER));
        assertFalse(Tier.BRONZE.isAtLeast(Tier.SILVER));
    }

    @Test
    void everyTierHasAReadableLabel() {
        assertEquals("Bronze", Tier.BRONZE.getLabel());
        assertEquals("Silver", Tier.SILVER.getLabel());
        assertEquals("Gold", Tier.GOLD.getLabel());
    }
}
