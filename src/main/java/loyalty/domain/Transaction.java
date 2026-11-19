package loyalty.domain;

import java.time.LocalDate;


public class Transaction {
    private final String date;
    private final int points;
    private final String description;

    public Transaction(int points, String description) {
        if (points == 0) {
            throw new IllegalArgumentException("A transaction cannot be 0 points");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("A transaction needs a description");
        }
        this.date = LocalDate.now().toString();
        this.points = points;
        this.description = description.trim();
    }

    public String getDate() {
        return this.date;
    }

    public int getPoints() {
        return this.points;
    }

    public String getDescription() {
        return this.description;
    }
}
