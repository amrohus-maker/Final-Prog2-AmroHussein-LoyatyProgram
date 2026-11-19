package loyalty.service;

import loyalty.domain.Customer;
import loyalty.domain.DiscountReward;
import loyalty.domain.FreeItemReward;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LoyaltyService {
    public static final double MAX_PURCHASE = 100000;

    private final List<Customer> customers = new ArrayList<>();
    private final List<Reward> rewards = new ArrayList<>();
    private int nextId = 1;

    public LoyaltyService() {
        // the reward catalog, cheapest first
        this.rewards.add(new FreeItemReward("Free coffee", 60, Tier.BRONZE, "coffee"));
        this.rewards.add(new FreeItemReward("Free pastry", 120, Tier.BRONZE, "pastry"));
        this.rewards.add(new DiscountReward("10% discount", 150, Tier.BRONZE, 10));
        this.rewards.add(new FreeItemReward("Free lunch", 350, Tier.SILVER, "lunch menu"));
        this.rewards.add(new DiscountReward("25% discount", 500, Tier.GOLD, 25));
        this.rewards.add(new FreeItemReward("Gift box", 700, Tier.GOLD, "gift box"));
    }

    public Customer register(String name, String email)
            throws InvalidNameException, InvalidEmailException, DuplicateEmailException {
        Customer customer = new Customer(this.nextId, name, email); // checks the name and the email
        for (Customer other : this.customers) {
            if (other.getEmail().equals(customer.getEmail())) {
                throw new DuplicateEmailException(customer.getEmail());
            }
        }
        customer.earn(Customer.WELCOME_BONUS, "Welcome bonus");
        this.customers.add(customer);
        this.nextId++;
        return customer;
    }

    public Customer findCustomer(int id) throws CustomerNotFoundException {
        for (Customer customer : this.customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        throw new CustomerNotFoundException("id " + id);
    }

    public Customer findCustomer(String email) throws CustomerNotFoundException {
        String wanted = "";
        if (email != null) {
            wanted = email.trim().toLowerCase();
        }
        for (Customer customer : this.customers) {
            if (customer.getEmail().equals(wanted)) {
                return customer;
            }
        }
        throw new CustomerNotFoundException("email " + wanted);
    }

    // Returns the points that were earned. The bonus uses the tier the customer has BEFORE this purchase.
    public int recordPurchase(Customer customer, double amount) throws InvalidAmountException {
        if (Double.isNaN(amount) || amount <= 0 || amount > MAX_PURCHASE) {
            throw new InvalidAmountException();
        }
        int base = (int) amount; // 1 point for every whole currency unit, the cents are dropped
        int bonus = base * customer.getTier().getBonusPercent() / 100;
        int points = base + bonus;
        if (points > 0) {
            String text = String.format(Locale.US, "Purchase $%.2f", amount);
            customer.earn(points, text);
        }
        return points;
    }

    // rewardNumber is the number in the catalog, the first reward is 1
    public String redeem(Customer customer, int rewardNumber)
            throws RewardNotFoundException, TierTooLowException, InsufficientPointsException {
        if (rewardNumber < 1 || rewardNumber > this.rewards.size()) {
            throw new RewardNotFoundException(rewardNumber);
        }
        Reward reward = this.rewards.get(rewardNumber - 1);
        return reward.redeem(customer);
    }

    // The lists below are copies, so nobody outside can change our own lists.
    public List<Customer> getCustomers() {
        return new ArrayList<>(this.customers);
    }

    public List<Reward> getRewards() {
        return new ArrayList<>(this.rewards);
    }

    // sorted with the natural order of Customer (Comparable): most points earned first
    public List<Customer> getLeaderboard() {
        List<Customer> copy = new ArrayList<>(this.customers);
        Collections.sort(copy);
        return copy;
    }

    // sorted with a Comparator: A to Z, upper and lower case do not matter
    public List<Customer> getCustomersByName() {
        List<Customer> copy = new ArrayList<>(this.customers);
        Comparator<Customer> byName = Comparator.comparing(c -> c.getName().toLowerCase());
        copy.sort(byName);
        return copy;
    }

    // sorted with a lambda: newest members first
    public List<Customer> getCustomersByJoinDate() {
        List<Customer> copy = new ArrayList<>(this.customers);
        copy.sort((a, b) -> b.getJoinedOn().compareTo(a.getJoinedOn()));
        return copy;
    }

    public List<Customer> search(String text) {
        List<Customer> found = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return found;
        }
        String wanted = text.trim().toLowerCase();
        for (Customer customer : this.customers) {
            if (customer.getName().toLowerCase().contains(wanted) || customer.getEmail().contains(wanted)) {
                found.add(customer);
            }
        }
        return found;
    }
}
