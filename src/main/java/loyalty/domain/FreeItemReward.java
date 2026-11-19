package loyalty.domain;

public class FreeItemReward extends Reward {
    private final String item;

    public FreeItemReward(String name, int cost, Tier minimumTier, String item) {
        super(name, cost, minimumTier);
        if (item == null || item.trim().isEmpty()) {
            throw new IllegalArgumentException("A free item reward needs an item");
        }
        this.item = item.trim();
    }

    @Override
    public String getBenefitText() {
        return "One free " + this.item;
    }
}
