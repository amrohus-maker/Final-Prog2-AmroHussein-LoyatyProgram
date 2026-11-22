package loyalty.ui;

import loyalty.domain.Customer;
import loyalty.domain.Reward;
import loyalty.domain.Tier;
import loyalty.domain.Transaction;
import loyalty.exception.CustomerNotFoundException;
import loyalty.exception.InsufficientPointsException;
import loyalty.exception.InvalidAmountException;
import loyalty.exception.InvalidChoiceException;
import loyalty.exception.LoyaltyException;
import loyalty.service.LoyaltyService;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleApp {
    private final LoyaltyService service;
    private final Scanner in;
    private final PrintStream out;

    public ConsoleApp(LoyaltyService service, Scanner in, PrintStream out) {
        if (service == null || in == null || out == null) {
            throw new IllegalArgumentException("The service, the input and the output are needed");
        }
        this.service = service;
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            boolean running = true;
            while (running) {
                this.showMenu();
                String choice = this.ask("Your choice: ");
                running = this.handle(choice);
            }
        } catch (NoSuchElementException e) {
            this.out.println();
            this.out.println("The input has ended.");
        }
        this.out.println("Goodbye!");
    }

    private boolean handle(String choice) {
        if (choice.equals("0")) {
            return false;
        }
        try {
            this.doChoice(choice);
        } catch (InsufficientPointsException e) {
            this.out.println("[error] " + e.getMessage() + " (" + e.getMissing() + " more points needed)");
        } catch (LoyaltyException e) {
            this.out.println("[error] " + e.getMessage());
        }
        return true;
    }

    private void doChoice(String choice) throws LoyaltyException {
        switch (choice) {
            case "1":
                this.registerCustomer();
                break;
            case "2":
                this.recordPurchase();
                break;
            case "3":
                this.redeemReward();
                break;
            case "4":
                this.showCustomer();
                break;
            case "5":
                this.searchCustomers();
                break;
            case "6":
                this.listCustomers();
                break;
            case "7":
                this.showRewards();
                break;
            default:
                throw this.invalidChoice(choice);
        }
    }

    private void showMenu() {
        this.out.println();
        this.out.println("=== Loyalty Program ===");
        this.out.println("1. Register a customer");
        this.out.println("2. Record a purchase");
        this.out.println("3. Redeem a reward");
        this.out.println("4. Show a customer");
        this.out.println("5. Search customers");
        this.out.println("6. List customers");
        this.out.println("7. Show the rewards");
        this.out.println("0. Exit");
    }

    private void registerCustomer() throws LoyaltyException {
        String name = this.ask("Name: ");
        String email = this.ask("Email: ");
        Customer customer = this.service.register(name, email);
        this.out.println("Registered " + customer);
        this.out.println("The welcome bonus of " + Customer.WELCOME_BONUS + " points is already added.");
    }

    private void recordPurchase() throws LoyaltyException {
        Customer customer = this.askCustomer();
        this.out.println("Customer: " + customer);
        double amount = this.askAmount();
        Tier tierBefore = customer.getTier();
        int points = this.service.recordPurchase(customer, amount);
        this.out.println("Points earned: " + points);
        this.out.println("New balance: " + customer.getPoints() + " points");
        if (customer.getTier() != tierBefore) {
            this.out.println("Level up! " + customer.getName() + " is now " + customer.getTier().getLabel() + ".");
        }
    }

    private void redeemReward() throws LoyaltyException {
        Customer customer = this.askCustomer();
        this.out.println("Customer: " + customer);
        this.showRewards();
        int number = this.askNumber("Reward number: ");
        String benefit = this.service.redeem(customer, number);
        this.out.println("Redeemed: " + benefit);
        this.out.println("New balance: " + customer.getPoints() + " points");
    }

    private void showCustomer() throws CustomerNotFoundException {
        Customer customer = this.askCustomer();
        this.out.println();
        this.out.println("#" + customer.getId() + " " + customer.getName());
        this.out.println("Email:     " + customer.getEmail());
        this.out.println("Joined on: " + customer.getJoinedOn());
        this.out.println("Tier:      " + customer.getTier().getLabel());
        this.out.println("Points:    " + customer.getPoints() + " (" + customer.getTotalEarned() + " earned in total)");
        this.out.println("History:");
        for (Transaction line : customer.getHistory()) {
            this.out.println(String.format(Locale.US, "  %s  %+5d  %s",
                    line.getDate(), line.getPoints(), line.getDescription()));
        }
    }

    private void searchCustomers() {
        String text = this.ask("Search for (part of a name or email): ");
        List<Customer> found = this.service.search(text);
        if (found.isEmpty()) {
            this.out.println("No customers found.");
        } else {
            this.out.println(found.size() + " found:");
            this.printCustomers(found);
        }
    }

    private void listCustomers() throws InvalidChoiceException {
        this.out.println("1. Leaderboard (most points earned first)");
        this.out.println("2. A to Z by name");
        this.out.println("3. Newest members first");
        String choice = this.ask("Your choice: ");
        List<Customer> customers;
        if (choice.equals("1")) {
            customers = this.service.getLeaderboard();
        } else if (choice.equals("2")) {
            customers = this.service.getCustomersByName();
        } else if (choice.equals("3")) {
            customers = this.service.getCustomersByJoinDate();
        } else {
            throw this.invalidChoice(choice);
        }
        if (customers.isEmpty()) {
            this.out.println("There are no customers yet.");
        } else {
            this.printCustomers(customers);
        }
    }

    private void showRewards() {
        this.out.println("Rewards:");
        int number = 1;
        for (Reward reward : this.service.getRewards()) {
            this.out.println(String.format(Locale.US, "%3d. %-14s %4d points  needs %-6s  %s",
                    number, reward.getName(), reward.getCost(), reward.getMinimumTier().getLabel(),
                    reward.getBenefitText()));
            number++;
        }
    }

    private void printCustomers(List<Customer> customers) {
        int number = 1;
        for (Customer customer : customers) {
            this.out.println(String.format(Locale.US, "%3d. %s - %d earned in total - %s",
                    number, customer, customer.getTotalEarned(), customer.getEmail()));
            number++;
        }
    }

    private Customer askCustomer() throws CustomerNotFoundException {
        String text = this.ask("Customer id or email: ");
        if (text.isEmpty()) {
            throw new CustomerNotFoundException("(nothing)");
        }
        try {
            int id = Integer.parseInt(text);
            return this.service.findCustomer(id);
        } catch (NumberFormatException e) {
            return this.service.findCustomer(text);
        }
    }

    private double askAmount() throws InvalidAmountException {
        String text = this.ask("Purchase amount: ");
        try {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new InvalidAmountException();
        }
    }

    private int askNumber(String question) throws InvalidChoiceException {
        String text = this.ask(question);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw this.invalidChoice(text);
        }
    }

    private InvalidChoiceException invalidChoice(String text) {
        if (text.isEmpty()) {
            return new InvalidChoiceException("(nothing)");
        }
        return new InvalidChoiceException(text);
    }

    private String ask(String question) {
        this.out.print(question);
        this.out.flush();
        return this.in.nextLine().trim();
    }
}
