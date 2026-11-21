package loyalty.domain;

import java.time.LocalDate;


public class Transaction {
    // not final, because Gson fills these fields when it loads the JSON file
    private String date;
    private int points;
    private String description;

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

    // used to check data that was loaded from a file
    public boolean isValid() {
        return this.points != 0
                && this.date != null && !this.date.trim().isEmpty()
                && this.description != null && !this.description.trim().isEmpty();
    }
}
