package loyalty.domain;

// A reward that gives a percent discount on the next order.
public class DiscountReward extends Reward {
    private final int percent;

    public DiscountReward(String name, int cost, Tier minimumTier, int percent) {
        super(name, cost, minimumTier);
        if (percent < 1 || percent > 100) {
            throw new IllegalArgumentException("The discount must be between 1 and 100 percent");
        }
        this.percent = percent;
    }

    public int getPercent() {
        return this.percent;
    }

    @Override
    public String getBenefitText() {
        return this.percent + "% off your next order";
    }
}
