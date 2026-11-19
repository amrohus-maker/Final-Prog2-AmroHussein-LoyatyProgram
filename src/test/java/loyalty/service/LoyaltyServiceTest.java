package loyalty.service;

import loyalty.domain.Customer;
import loyalty.domain.Reward;
import loyalty.domain.Tier;
import loyalty.exception.CustomerNotFoundException;
import loyalty.exception.DuplicateEmailException;
import loyalty.exception.InsufficientPointsException;
import loyalty.exception.InvalidAmountException;
import loyalty.exception.InvalidEmailException;
import loyalty.exception.InvalidNameException;
import loyalty.exception.RewardNotFoundException;
import loyalty.exception.TierTooLowException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoyaltyServiceTest {
    private LoyaltyService service;

    @BeforeEach
    void setUp() {
        service = new LoyaltyService();
    }

    // ---------- register ----------

    @Test
    void registeringGivesTheWelcomeBonusAndIdsCountUp() throws Exception {
        Customer first = service.register("Ann Lee", "ann@example.com");
        Customer second = service.register("Bob Ray", "bob@example.com");

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(Customer.WELCOME_BONUS, first.getPoints());
        assertEquals("Welcome bonus", first.getHistory().get(0).getDescription());
        assertEquals(2, service.getCustomers().size());
    }

    @Test
    void anEmptyNameIsRefused() {
        assertThrows(InvalidNameException.class, () -> service.register("  ", "ann@example.com"));
        assertEquals(0, service.getCustomers().size());
    }

    @Test
    void aBadEmailIsRefused() {
        assertThrows(InvalidEmailException.class, () -> service.register("Ann Lee", "not-an-email"));
        assertEquals(0, service.getCustomers().size());
    }

    @Test
    void theSameEmailTwiceIsRefusedEvenWithOtherCapitals() throws Exception {
        service.register("Ann Lee", "ann@example.com");
        assertThrows(DuplicateEmailException.class, () -> service.register("Other Ann", "ANN@example.com"));
        assertEquals(1, service.getCustomers().size());
    }

    @Test
    void aFailedRegistrationDoesNotUseUpAnId() throws Exception {
        assertThrows(InvalidEmailException.class, () -> service.register("Ann Lee", "nope"));
        assertEquals(1, service.register("Ann Lee", "ann@example.com").getId());
    }

    // ---------- find ----------

    @Test
    void aCustomerCanBeFoundByIdOrByEmail() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        assertSame(ann, service.findCustomer(1));
        assertSame(ann, service.findCustomer("  Ann@Example.com "));
    }

    @Test
    void findingNobodyThrowsCustomerNotFound() {
        assertThrows(CustomerNotFoundException.class, () -> service.findCustomer(42));
        assertThrows(CustomerNotFoundException.class, () -> service.findCustomer("nobody@example.com"));
        assertThrows(CustomerNotFoundException.class, () -> service.findCustomer((String) null));
    }

    // ---------- purchases ----------

    @Test
    void aBronzePurchaseGivesOnePointForEveryWholeUnit() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com"); // 50 welcome points

        assertEquals(20, service.recordPurchase(ann, 20.0));
        assertEquals(12, service.recordPurchase(ann, 12.99)); // the cents are dropped
        assertEquals(50 + 20 + 12, ann.getPoints());
    }

    @Test
    void silverAndGoldCustomersGetABonus() throws Exception {
        Customer silver = service.register("Sil Ver", "silver@example.com");
        silver.earn(250, "test"); // 300 points earned: Silver
        assertEquals(Tier.SILVER, silver.getTier());
        assertEquals(110, service.recordPurchase(silver, 100.0)); // 100 + 10%

        Customer gold = service.register("Gol Den", "gold@example.com");
        gold.earn(950, "test"); // 1000 points earned: Gold
        assertEquals(Tier.GOLD, gold.getTier());
        assertEquals(125, service.recordPurchase(gold, 100.0)); // 100 + 25%
    }

    @Test
    void aPurchaseUnderOneUnitGivesNothingAndLeavesNoHistoryLine() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        assertEquals(0, service.recordPurchase(ann, 0.5));
        assertEquals(1, ann.getHistory().size()); // only the welcome bonus
    }

    @Test
    void wrongAmountsAreRefusedAndChangeNothing() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        double[] bad = {0, -5, Double.NaN, Double.POSITIVE_INFINITY, 100000.01};
        for (double amount : bad) {
            assertThrows(InvalidAmountException.class, () -> service.recordPurchase(ann, amount),
                    "should have been refused: " + amount);
        }
        assertEquals(50, ann.getPoints());
    }

    @Test
    void theBiggestAllowedPurchaseWorks() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        assertEquals(100000, service.recordPurchase(ann, 100000));
    }

    @Test
    void enoughPurchasesMoveACustomerUpATier() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        assertEquals(Tier.BRONZE, ann.getTier());
        service.recordPurchase(ann, 250); // 50 + 250 = 300 points earned
        assertEquals(Tier.SILVER, ann.getTier());
    }

    // ---------- redeem ----------

    @Test
    void redeemingByNumberTakesThePointsAndReturnsTheBenefit() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        ann.earn(70, "test"); // 120 points

        String benefit = service.redeem(ann, 1); // reward 1 is the free coffee for 60 points

        assertEquals("One free coffee", benefit);
        assertEquals(60, ann.getPoints());
    }

    @Test
    void anUnknownRewardNumberThrows() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        int size = service.getRewards().size();
        assertThrows(RewardNotFoundException.class, () -> service.redeem(ann, 0));
        assertThrows(RewardNotFoundException.class, () -> service.redeem(ann, -1));
        assertThrows(RewardNotFoundException.class, () -> service.redeem(ann, size + 1));
        assertEquals(50, ann.getPoints());
    }

    @Test
    void tooFewPointsThrowsAndChangesNothing() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com"); // 50 points, the coffee costs 60
        InsufficientPointsException e = assertThrows(InsufficientPointsException.class, () -> service.redeem(ann, 1));
        assertEquals(10, e.getMissing());
        assertEquals(50, ann.getPoints());
    }

    @Test
    void aTierThatIsTooLowThrowsEvenWithEnoughPoints() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        ann.earn(350, "test"); // 400 points: Silver, the 25% discount (number 5) is for Gold
        TierTooLowException e = assertThrows(TierTooLowException.class, () -> service.redeem(ann, 5));
        assertEquals("Gold", e.getTierName());
        assertEquals(400, ann.getPoints());
    }

    @Test
    void theCatalogIsInCostOrderAndIsACopy() {
        List<Reward> rewards = service.getRewards();
        assertEquals(6, rewards.size());
        for (int i = 1; i < rewards.size(); i++) {
            assertTrue(rewards.get(i - 1).getCost() <= rewards.get(i).getCost());
        }
        rewards.clear();
        assertEquals(6, service.getRewards().size()); // our own catalog is untouched
    }

    // ---------- lists, sorting and search ----------

    @Test
    void theCustomerListIsACopy() throws Exception {
        service.register("Ann Lee", "ann@example.com");
        List<Customer> copy = service.getCustomers();
        copy.clear();
        assertEquals(1, service.getCustomers().size());
    }

    @Test
    void theLeaderboardPutsTheMostPointsFirst() throws Exception {
        Customer ann = service.register("Ann", "ann@example.com");
        Customer bob = service.register("Bob", "bob@example.com");
        Customer cy = service.register("Cy", "cy@example.com");
        bob.earn(500, "test");
        cy.earn(100, "test");

        List<Customer> board = service.getLeaderboard();

        assertSame(bob, board.get(0));
        assertSame(cy, board.get(1));
        assertSame(ann, board.get(2));
    }

    @Test
    void theNameListGoesFromAToZWhateverTheCapitals() throws Exception {
        Customer zoe = service.register("zoe", "zoe@example.com");
        Customer adam = service.register("Adam", "adam@example.com");
        Customer mia = service.register("Mia", "mia@example.com");

        List<Customer> list = service.getCustomersByName();

        assertSame(adam, list.get(0));
        assertSame(mia, list.get(1));
        assertSame(zoe, list.get(2));
    }

    @Test
    void theJoinDateListHasEveryoneInIt() throws Exception {
        service.register("Ann", "ann@example.com");
        service.register("Bob", "bob@example.com");
        service.register("Cy", "cy@example.com");
        assertEquals(3, service.getCustomersByJoinDate().size());
    }

    @Test
    void sortingNeverChangesTheOrderInsideTheService() throws Exception {
        Customer zed = service.register("Zed", "zed@example.com");
        Customer abe = service.register("Abe", "abe@example.com");
        abe.earn(500, "test");

        service.getLeaderboard();
        service.getCustomersByName();
        service.getCustomersByJoinDate();

        List<Customer> inOrderOfRegistration = service.getCustomers();
        assertSame(zed, inOrderOfRegistration.get(0));
        assertSame(abe, inOrderOfRegistration.get(1));
    }

    @Test
    void searchFindsPartOfANameOrAnEmail() throws Exception {
        service.register("Ann Lee", "ann@example.com");
        service.register("Bob Ray", "bob@work.org");

        assertEquals(1, service.search("lee").size());
        assertEquals(1, service.search("  WORK ").size());
        assertEquals(2, service.search("a").size()); // "Ann" and "Ray"
        assertEquals(0, service.search("zzz").size());
    }

    @Test
    void aBlankSearchFindsNobody() throws Exception {
        service.register("Ann Lee", "ann@example.com");
        assertEquals(0, service.search("   ").size());
        assertEquals(0, service.search(null).size());
    }
}
