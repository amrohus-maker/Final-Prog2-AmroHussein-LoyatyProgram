package loyalty.domain;

import loyalty.exception.InsufficientPointsException;
import loyalty.exception.TierTooLowException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardTest {
    // a customer who has earned this many points (and has not spent any)
    private Customer customerWith(int points) throws Exception {
        Customer customer = new Customer(1, "Ann Lee", "ann@example.com");
        if (points > 0) {
            customer.earn(points, "test points");
        }
        return customer;
    }

    @Test
    void everyKindOfRewardWordsItsBenefitInItsOwnWay() {
        // both are used through the parent type Reward
        List<Reward> rewards = new ArrayList<>();
        rewards.add(new FreeItemReward("Espresso on us", 60, Tier.BRONZE, "Espresso"));
        rewards.add(new DiscountReward("Small saver", 150, Tier.BRONZE, 10));

        assertEquals("One free Espresso", rewards.get(0).getBenefitText());
        assertEquals("10% off your next order", rewards.get(1).getBenefitText());
    }

    @Test
    void redeemTakesThePointsAndReturnsTheBenefit() throws Exception {
        Customer customer = customerWith(100);
        Reward reward = new FreeItemReward("Espresso on us", 60, Tier.BRONZE, "Espresso");

        String benefit = reward.redeem(customer);

        assertEquals("One free Espresso", benefit);
        assertEquals(40, customer.getPoints());
        List<Transaction> history = customer.getHistory();
        assertTrue(history.get(history.size() - 1).getDescription().contains("Espresso on us"));
    }

    @Test
    void theSameRedeemStepsWorkForEveryKindOfReward() throws Exception {
        List<Reward> rewards = new ArrayList<>();
        rewards.add(new FreeItemReward("Espresso on us", 60, Tier.BRONZE, "Espresso"));
        rewards.add(new DiscountReward("Small saver", 150, Tier.BRONZE, 10));

        for (Reward reward : rewards) {
            Customer customer = customerWith(200);
            reward.redeem(customer);
            assertEquals(200 - reward.getCost(), customer.getPoints());
        }
    }

    @Test
    void tooFewPointsThrowsAndChangesNothing() throws Exception {
        Customer customer = customerWith(50);
        Reward reward = new FreeItemReward("Espresso on us", 60, Tier.BRONZE, "Espresso");

        InsufficientPointsException e = assertThrows(InsufficientPointsException.class, () -> reward.redeem(customer));

        assertEquals(10, e.getMissing());
        assertEquals(50, customer.getPoints());
    }

    @Test
    void aTierThatIsTooLowThrowsEvenWithEnoughPoints() throws Exception {
        Customer customer = customerWith(400); // 400 points earned: Silver, but not Gold
        Reward goldReward = new DiscountReward("Big saver", 100, Tier.GOLD, 25);

        TierTooLowException e = assertThrows(TierTooLowException.class, () -> goldReward.redeem(customer));

        assertEquals("Gold", e.getTierName());
        assertEquals(400, customer.getPoints());
    }

    @Test
    void aGoldRewardWorksForAGoldCustomer() throws Exception {
        Customer customer = customerWith(1000);
        Reward goldReward = new DiscountReward("Big saver", 500, Tier.GOLD, 25);

        assertEquals("25% off your next order", goldReward.redeem(customer));
        assertEquals(500, customer.getPoints());
    }

    @Test
    void wrongRewardsAreRefusedWhenTheyAreCreated() {
        assertThrows(IllegalArgumentException.class, () -> new FreeItemReward("", 60, Tier.BRONZE, "Espresso"));
        assertThrows(IllegalArgumentException.class, () -> new FreeItemReward("Free", 0, Tier.BRONZE, "Espresso"));
        assertThrows(IllegalArgumentException.class, () -> new FreeItemReward("Free", 60, null, "Espresso"));
        assertThrows(IllegalArgumentException.class, () -> new FreeItemReward("Free", 60, Tier.BRONZE, "  "));
        assertThrows(IllegalArgumentException.class, () -> new DiscountReward("Sale", 100, Tier.BRONZE, 0));
        assertThrows(IllegalArgumentException.class, () -> new DiscountReward("Sale", 100, Tier.BRONZE, 101));
    }
}
