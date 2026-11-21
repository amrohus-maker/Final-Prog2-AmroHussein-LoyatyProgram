package loyalty.domain;

import loyalty.exception.InsufficientPointsException;
import loyalty.exception.InvalidEmailException;
import loyalty.exception.InvalidNameException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Customer implements Comparable<Customer> {
    public static final int WELCOME_BONUS = 50;

    // not final, because Gson fills these fields when it loads the JSON file
    private int id;
    private String name;
    private String email;
    private String joinedOn;           // text like 2026-11-19
    private int points;                // what the customer can spend now
    private int totalEarned;           // everything ever earned, decides the tier
    private List<Transaction> history = new ArrayList<>();

    public Customer(int id, String name, String email) throws InvalidNameException, InvalidEmailException {
        if (id <= 0) {
            throw new IllegalArgumentException("The id must be above 0");
        }
        this.id = id;
        this.name = checkName(name);
        this.email = checkEmail(email);
        this.joinedOn = LocalDate.now().toString();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getJoinedOn() {
        return this.joinedOn;
    }

    public int getPoints() {
        return this.points;
    }

    public int getTotalEarned() {
        return this.totalEarned;
    }

    public Tier getTier() {
        return Tier.forTotalPoints(this.totalEarned);
    }

    // We return a copy, so nobody outside can change our history.
    public List<Transaction> getHistory() {
        return new ArrayList<>(this.history);
    }

    // Gson does not call the constructor when it loads a file, so we use this to check the loaded data.
    public boolean isValid() {
        if (this.id <= 0 || this.joinedOn == null || this.joinedOn.trim().isEmpty() || this.history == null) {
            return false;
        }
        try {
            if (!checkName(this.name).equals(this.name) || !checkEmail(this.email).equals(this.email)) {
                return false;
            }
        } catch (InvalidNameException | InvalidEmailException e) {
            return false;
        }
        int balance = 0;
        int earned = 0;
        for (Transaction line : this.history) {
            if (line == null || !line.isValid()) {
                return false;
            }
            balance += line.getPoints();
            if (line.getPoints() > 0) {
                earned += line.getPoints();
            }
        }
        // the points and the total must be what the history adds up to
        return balance == this.points && earned == this.totalEarned && this.points >= 0;
    }

    // The service checks the purchase amount first, so a wrong number of points
    // here would be a mistake in our own code. That is why it is an IllegalArgumentException.
    public void earn(int amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Points to earn must be above 0");
        }
        Transaction line = new Transaction(amount, description); // made first, in case the description is wrong
        this.points += amount;
        this.totalEarned += amount;
        this.history.add(line);
    }

    public void spend(int amount, String description) throws InsufficientPointsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Points to spend must be above 0");
        }
        if (amount > this.points) {
            throw new InsufficientPointsException(amount, this.points);
        }
        Transaction line = new Transaction(-amount, description);
        this.points -= amount;
        this.history.add(line);
    }

    // The natural order is the leaderboard order: most points earned first,
    // and if two customers are equal, by name.
    @Override
    public int compareTo(Customer other) {
        int result = Integer.compare(other.totalEarned, this.totalEarned);
        if (result == 0) {
            result = this.name.compareToIgnoreCase(other.name);
        }
        return result;
    }

    @Override
    public String toString() {
        return "#" + this.id + " " + this.name + " (" + this.getTier().getLabel() + ", " + this.points + " points)";
    }

    private static String checkName(String name) throws InvalidNameException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidNameException();
        }
        return name.trim();
    }

    private static String checkEmail(String email) throws InvalidEmailException {
        if (email == null) {
            throw new InvalidEmailException("(nothing)");
        }
        String text = email.trim().toLowerCase();
        int at = text.indexOf('@');
        int dot = text.lastIndexOf('.');
        boolean oneAt = at > 0 && text.indexOf('@', at + 1) == -1;
        boolean dotAfterAt = dot > at + 1 && dot < text.length() - 1;
        if (!oneAt || !dotAfterAt || text.contains(" ")) {
            throw new InvalidEmailException(email);
        }
        return text;
    }
}
