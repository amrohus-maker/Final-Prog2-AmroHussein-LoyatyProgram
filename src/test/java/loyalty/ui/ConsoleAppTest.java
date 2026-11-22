package loyalty.ui;

import loyalty.domain.Customer;
import loyalty.exception.StorageException;
import loyalty.persistence.DataStorage;
import loyalty.persistence.MemoryStorageFactory;
import loyalty.service.LoyaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleAppTest {
    private static final String MENU_TITLE = "=== Loyalty Program ===";
    private static final String AMOUNT_ERROR = "[error] The amount must be above 0 and not more than 100000";

    private LoyaltyService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new LoyaltyService(new MemoryStorageFactory().createStorage());
    }

    private String run(String... lines) {
        return run(service, lines);
    }

    private String run(LoyaltyService whichService, String... lines) {
        String input = String.join("\n", lines) + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        new ConsoleApp(whichService, new Scanner(input), out).run();
        out.flush();
        return bytes.toString();
    }

    private static int count(String text, String part) {
        int found = 0;
        int index = text.indexOf(part);
        while (index != -1) {
            found++;
            index = text.indexOf(part, index + part.length());
        }
        return found;
    }

    private static class FailingStorage implements DataStorage {
        @Override
        public void save(List<Customer> customers) throws StorageException {
            throw new StorageException("The disk is full");
        }

        @Override
        public List<Customer> load() {
            return new ArrayList<>();
        }
    }

    @Test
    void exitShowsTheMenuAndSaysGoodbye() {
        String output = run("0");

        assertTrue(output.contains(MENU_TITLE));
        assertTrue(output.contains("0. Exit"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void theConstructorNeedsAServiceAnInputAndAnOutput() {
        assertThrows(IllegalArgumentException.class, () -> new ConsoleApp(null, new Scanner(""), System.out));
        assertThrows(IllegalArgumentException.class, () -> new ConsoleApp(service, null, System.out));
        assertThrows(IllegalArgumentException.class, () -> new ConsoleApp(service, new Scanner(""), null));
    }

    @Test
    void aWrongMenuChoiceShowsTheErrorAndTheMenuComesBack() {
        String output = run("9", "0");

        assertTrue(output.contains("[error] That is not a choice in the menu: 9"));
        assertEquals(2, count(output, MENU_TITLE));
    }

    @Test
    void anEmptyMenuChoiceIsAlsoAWrongChoice() {
        assertTrue(run("", "0").contains("[error] That is not a choice in the menu: (nothing)"));
    }

    @Test
    void whenTheInputEndsTheProgramStopsWithoutCrashing() {
        String output = run("1", "Ann Lee");

        assertTrue(output.contains("The input has ended."));
        assertTrue(output.contains("Goodbye!"));
        assertEquals(0, service.getCustomers().size());
    }

    @Test
    void registeringACustomerShowsTheNewCustomer() {
        String output = run("1", "Ann Lee", "ann@example.com", "0");

        assertTrue(output.contains("Registered #1 Ann Lee (Bronze, 50 points)"));
        assertTrue(output.contains("welcome bonus of 50 points"));
        assertEquals(1, service.getCustomers().size());
    }

    @Test
    void registeringWithAnEmptyNameShowsTheError() {
        String output = run("1", "   ", "ann@example.com", "0");

        assertTrue(output.contains("[error] The name cannot be empty"));
        assertEquals(0, service.getCustomers().size());
    }

    @Test
    void registeringWithABadEmailShowsTheError() {
        String output = run("1", "Ann Lee", "nope", "0");

        assertTrue(output.contains("[error] That does not look like an email address: nope"));
        assertEquals(0, service.getCustomers().size());
    }

    @Test
    void registeringTheSameEmailTwiceShowsTheError() {
        String output = run("1", "Ann Lee", "ann@example.com", "1", "Other Ann", "ANN@example.com", "0");

        assertTrue(output.contains("[error] That email is already registered: ann@example.com"));
        assertEquals(1, service.getCustomers().size());
    }

    @Test
    void aPurchaseShowsThePointsAndTheNewBalance() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("2", "1", "100", "0");

        assertTrue(output.contains("Customer: #1 Ann Lee (Bronze, 50 points)"));
        assertTrue(output.contains("Points earned: 100"));
        assertTrue(output.contains("New balance: 150 points"));
    }

    @Test
    void aCustomerCanBeFoundByEmailEvenWithCapitalsAndSpaces() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("2", "  ANN@example.com ", "20", "0");

        assertTrue(output.contains("Points earned: 20"));
    }

    @Test
    void aPurchaseThatReachesSilverShowsALevelUp() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("2", "1", "250", "0");

        assertTrue(output.contains("Level up! Ann Lee is now Silver."));
    }

    @Test
    void aPurchaseThatStaysInTheSameTierShowsNoLevelUp() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("2", "1", "10", "0");

        assertFalse(output.contains("Level up"));
    }

    @Test
    void aPurchaseAmountThatIsNotANumberShowsTheError() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("2", "1", "abc", "0");

        assertTrue(output.contains(AMOUNT_ERROR));
        assertEquals(50, service.findCustomer(1).getPoints());
    }

    @Test
    void aPurchaseAmountThatIsTooSmallOrTooBigShowsTheError() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("2", "1", "0", "2", "1", "-5", "2", "1", "100001", "0");

        assertEquals(3, count(output, AMOUNT_ERROR));
        assertEquals(50, service.findCustomer(1).getPoints());
    }

    @Test
    void anAmountWithACommaInsteadOfADotIsAccepted() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("2", "1", "12,50", "0");

        assertTrue(output.contains("Points earned: 12"));
    }

    @Test
    void anUnknownIdShowsTheError() {
        String output = run("2", "99", "0");

        assertTrue(output.contains("[error] No customer found for id 99"));
    }

    @Test
    void anUnknownEmailShowsTheError() {
        String output = run("2", "nobody@example.com", "0");

        assertTrue(output.contains("[error] No customer found for email nobody@example.com"));
    }

    @Test
    void anEmptyCustomerAnswerShowsTheError() {
        String output = run("4", "", "0");

        assertTrue(output.contains("[error] No customer found for (nothing)"));
    }

    @Test
    void redeemingARewardShowsTheBenefitAndTheNewBalance() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        service.recordPurchase(ann, 100);

        String output = run("3", "1", "1", "0");

        assertTrue(output.contains("Redeemed: One free coffee"));
        assertTrue(output.contains("New balance: 90 points"));
    }

    @Test
    void redeemingWithTooFewPointsShowsHowManyAreMissing() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("3", "1", "1", "0");

        assertTrue(output.contains("[error] Needs 60 points but only 50 are available (10 more points needed)"));
        assertEquals(50, service.findCustomer(1).getPoints());
    }

    @Test
    void redeemingAGoldRewardAsASilverCustomerShowsTheTierError() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        service.recordPurchase(ann, 600);

        String output = run("3", "1", "5", "0");

        assertTrue(output.contains("[error] This reward needs the Gold tier or higher"));
        assertEquals(650, ann.getPoints());
    }

    @Test
    void anUnknownRewardNumberShowsTheError() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("3", "1", "9", "0");

        assertTrue(output.contains("[error] There is no reward with the number 9"));
    }

    @Test
    void aRewardAnswerThatIsNotANumberShowsTheError() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        String output = run("3", "1", "coffee", "0");

        assertTrue(output.contains("[error] That is not a choice in the menu: coffee"));
        assertEquals(50, service.findCustomer(1).getPoints());
    }

    @Test
    void showingACustomerListsTheProfileAndTheHistory() throws Exception {
        Customer ann = service.register("Ann Lee", "ann@example.com");
        service.recordPurchase(ann, 100);

        String output = run("4", "1", "0");

        assertTrue(output.contains("#1 Ann Lee"));
        assertTrue(output.contains("ann@example.com"));
        assertTrue(output.contains("Tier:      Bronze"));
        assertTrue(output.contains("150 (150 earned in total)"));
        assertTrue(output.contains("+50  Welcome bonus"));
        assertTrue(output.contains("+100  Purchase $100.00"));
    }

    @Test
    void searchingFindsPartOfANameOrAnEmail() throws Exception {
        service.register("Ann Lee", "ann@example.com");
        service.register("Bob Ray", "bob@example.com");

        String output = run("5", "ray", "0");

        assertTrue(output.contains("1 found:"));
        assertTrue(output.contains("Bob Ray"));
        assertFalse(output.contains("Ann Lee"));
    }

    @Test
    void searchingForSomethingThatDoesNotExistSaysSo() throws Exception {
        service.register("Ann Lee", "ann@example.com");

        assertTrue(run("5", "zzz", "0").contains("No customers found."));
    }

    @Test
    void theLeaderboardShowsTheBestCustomerFirst() throws Exception {
        service.register("Ann Lee", "ann@example.com");
        Customer bob = service.register("Bob Ray", "bob@example.com");
        service.recordPurchase(bob, 100);

        String output = run("6", "1", "0");

        assertTrue(output.indexOf("Bob Ray") < output.indexOf("Ann Lee"));
    }

    @Test
    void theNameListGoesFromAToZ() throws Exception {
        service.register("Cara Poe", "cara@example.com");
        service.register("Ann Lee", "ann@example.com");
        service.register("Bob Ray", "bob@example.com");

        String output = run("6", "2", "0");

        assertTrue(output.indexOf("Ann Lee") < output.indexOf("Bob Ray"));
        assertTrue(output.indexOf("Bob Ray") < output.indexOf("Cara Poe"));
    }

    @Test
    void theNewestMembersComeFirst() throws Exception {
        service.register("Ann Lee", "ann@example.com");
        service.register("Bob Ray", "bob@example.com");
        service.register("Cara Poe", "cara@example.com");

        String output = run("6", "3", "0");

        assertTrue(output.indexOf("Cara Poe") < output.indexOf("Bob Ray"));
        assertTrue(output.indexOf("Bob Ray") < output.indexOf("Ann Lee"));
    }

    @Test
    void listingWhenThereAreNoCustomersSaysSo() {
        assertTrue(run("6", "1", "0").contains("There are no customers yet."));
    }

    @Test
    void aWrongListChoiceShowsTheError() {
        assertTrue(run("6", "x", "0").contains("[error] That is not a choice in the menu: x"));
    }

    @Test
    void theRewardsAreShownWithCostTierAndBenefit() {
        String output = run("7", "0");

        assertTrue(output.contains("Free coffee"));
        assertTrue(output.contains("60 points"));
        assertTrue(output.contains("needs Gold"));
        assertTrue(output.contains("25% off your next order"));
    }

    @Test
    void aStorageErrorIsShownAndTheMenuComesBack() throws Exception {
        LoyaltyService brokenService = new LoyaltyService(new FailingStorage());

        String output = run(brokenService, "1", "Ann Lee", "ann@example.com", "0");

        assertTrue(output.contains("[error] The disk is full"));
        assertEquals(2, count(output, MENU_TITLE));
    }
}
