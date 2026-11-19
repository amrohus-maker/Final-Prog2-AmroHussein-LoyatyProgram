package loyalty.domain;

import loyalty.exception.InsufficientPointsException;
import loyalty.exception.InvalidEmailException;
import loyalty.exception.InvalidNameException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerTest {
    private Customer newCustomer() throws Exception {
        return new Customer(1, "Ann Lee", "ann@example.com");
    }

    @Test
    void aNewCustomerStartsWithNoPoints() throws Exception {
        Customer customer = newCustomer();
        assertEquals(1, customer.getId());
        assertEquals(0, customer.getPoints());
        assertEquals(0, customer.getTotalEarned());
        assertEquals(Tier.BRONZE, customer.getTier());
        assertTrue(customer.getHistory().isEmpty());
    }

    @Test
    void nameAndEmailAreCleanedUp() throws Exception {
        Customer customer = new Customer(1, "  Ann Lee ", "  Ann.Lee@Example.COM ");
        assertEquals("Ann Lee", customer.getName());
        assertEquals("ann.lee@example.com", customer.getEmail());
    }

    @Test
    void anEmptyNameIsRefused() {
        assertThrows(InvalidNameException.class, () -> new Customer(1, "   ", "ann@example.com"));
        assertThrows(InvalidNameException.class, () -> new Customer(1, null, "ann@example.com"));
    }

    @Test
    void badEmailsAreRefused() {
        String[] bad = {"", "plain", "a@b", "@example.com", "a b@example.com", "ann@example.", "ann@.com", "a@@b.com"};
        for (String email : bad) {
            assertThrows(InvalidEmailException.class, () -> new Customer(1, "Ann Lee", email),
                    "should have been refused: " + email);
        }
        assertThrows(InvalidEmailException.class, () -> new Customer(1, "Ann Lee", null));
    }

    @Test
    void theIdMustBeAboveZero() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(0, "Ann Lee", "ann@example.com"));
    }

    @Test
    void earningAddsToThePointsTheTotalAndTheHistory() throws Exception {
        Customer customer = newCustomer();
        customer.earn(40, "Purchase");
        customer.earn(10, "Purchase");
        assertEquals(50, customer.getPoints());
        assertEquals(50, customer.getTotalEarned());
        assertEquals(2, customer.getHistory().size());
    }

    @Test
    void earningZeroOrLessIsAMistakeInOurCode() throws Exception {
        Customer customer = newCustomer();
        assertThrows(IllegalArgumentException.class, () -> customer.earn(0, "nothing"));
        assertThrows(IllegalArgumentException.class, () -> customer.earn(-5, "negative"));
        assertEquals(0, customer.getPoints());
    }

    @Test
    void aWrongDescriptionChangesNoPoints() throws Exception {
        Customer customer = newCustomer();
        assertThrows(IllegalArgumentException.class, () -> customer.earn(10, ""));
        assertEquals(0, customer.getPoints());
        assertEquals(0, customer.getTotalEarned());
    }

    @Test
    void spendingTakesPointsButKeepsTheTotal() throws Exception {
        Customer customer = newCustomer();
        customer.earn(100, "Purchase");
        customer.spend(60, "Redeemed: Espresso");
        assertEquals(40, customer.getPoints());
        assertEquals(100, customer.getTotalEarned());
        assertEquals(-60, customer.getHistory().get(1).getPoints());
    }

    @Test
    void spendingMoreThanYouHaveThrowsAndChangesNothing() throws Exception {
        Customer customer = newCustomer();
        customer.earn(50, "Purchase");
        InsufficientPointsException e = assertThrows(InsufficientPointsException.class,
                () -> customer.spend(80, "Too much"));
        assertEquals(30, e.getMissing());
        assertEquals(50, customer.getPoints());
        assertEquals(1, customer.getHistory().size());
    }

    @Test
    void theTierFollowsTheTotalEarned() throws Exception {
        Customer customer = newCustomer();
        customer.earn(299, "Purchase");
        assertEquals(Tier.BRONZE, customer.getTier());
        customer.earn(1, "Purchase");
        assertEquals(Tier.SILVER, customer.getTier());
        customer.earn(700, "Purchase");
        assertEquals(Tier.GOLD, customer.getTier());
    }

    @Test
    void spendingNeverLowersTheTier() throws Exception {
        Customer customer = newCustomer();
        customer.earn(1000, "Big order");
        customer.spend(1000, "Redeemed: everything");
        assertEquals(0, customer.getPoints());
        assertEquals(Tier.GOLD, customer.getTier());
    }

    @Test
    void theHistoryWeGetIsACopy() throws Exception {
        Customer customer = newCustomer();
        customer.earn(10, "Purchase");
        List<Transaction> copy = customer.getHistory();
        copy.clear();
        assertEquals(1, customer.getHistory().size()); // our own list is untouched
    }

    @Test
    void theNaturalOrderIsMostPointsEarnedFirst() throws Exception {
        Customer low = new Customer(1, "Low", "low@example.com");
        Customer high = new Customer(2, "High", "high@example.com");
        Customer middle = new Customer(3, "Middle", "middle@example.com");
        low.earn(10, "x");
        high.earn(500, "x");
        middle.earn(100, "x");

        List<Customer> list = new ArrayList<>();
        list.add(low);
        list.add(high);
        list.add(middle);
        Collections.sort(list); // uses compareTo

        assertEquals("High", list.get(0).getName());
        assertEquals("Middle", list.get(1).getName());
        assertEquals("Low", list.get(2).getName());
    }

    @Test
    void equalPointsAreOrderedByName() throws Exception {
        Customer zoe = new Customer(1, "Zoe", "zoe@example.com");
        Customer adam = new Customer(2, "Adam", "adam@example.com");
        zoe.earn(50, "x");
        adam.earn(50, "x");

        List<Customer> list = new ArrayList<>();
        list.add(zoe);
        list.add(adam);
        Collections.sort(list);

        assertEquals("Adam", list.get(0).getName());
    }

    @Test
    void theWelcomeBonusIsFiftyPoints() {
        assertEquals(50, Customer.WELCOME_BONUS);
    }
}
