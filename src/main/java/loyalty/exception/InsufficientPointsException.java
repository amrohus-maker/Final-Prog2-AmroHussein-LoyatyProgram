package loyalty.exception;

// The customer wants to spend more points than they have.
public class InsufficientPointsException extends LoyaltyException {
    private final int needed;
    private final int available;

    public InsufficientPointsException(int needed, int available) {
        super("Needs " + needed + " points but only " + available + " are available");
        this.needed = needed;
        this.available = available;
    }

    public int getNeeded() {
        return this.needed;
    }

    public int getAvailable() {
        return this.available;
    }

    // how many points are still missing
    public int getMissing() {
        return this.needed - this.available;
    }
}
