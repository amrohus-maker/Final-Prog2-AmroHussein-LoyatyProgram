package loyalty.domain;

import loyalty.exception.InsufficientPointsException;
import loyalty.exception.TierTooLowException;


public abstract class Reward {
    private final String name;
    private final int cost;
    private final Tier minimumTier;

    public Reward(String name, int cost, Tier minimumTier) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("A reward needs a name");
        }
        if (cost <= 0) {
            throw new IllegalArgumentException("A reward must cost at least 1 point");
        }
        if (minimumTier == null) {
            throw new IllegalArgumentException("A reward needs a minimum tier");
        }
        this.name = name.trim();
        this.cost = cost;
        this.minimumTier = minimumTier;
    }

    public String getName() {
        return this.name;
    }

    public int getCost() {
        return this.cost;
    }

    public Tier getMinimumTier() {
        return this.minimumTier;
    }

    // "final" means no subclass can change these steps, so every reward is checked in the same way.
    public final String redeem(Customer customer) throws TierTooLowException, InsufficientPointsException {
        if (!customer.getTier().isAtLeast(this.minimumTier)) {
            throw new TierTooLowException(this.minimumTier.getLabel());
        }
        customer.spend(this.cost, "Redeemed: " + this.name); // throws InsufficientPointsException when too poor
        return this.getBenefitText();
    }

    // what the customer gets, every kind of reward words this in its own way
    public abstract String getBenefitText();
}
