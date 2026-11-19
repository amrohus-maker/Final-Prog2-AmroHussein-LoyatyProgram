package loyalty.domain;

public enum Tier {
    BRONZE("Bronze", 0, 0),
    SILVER("Silver", 300, 10),
    GOLD("Gold", 1000, 25);

    private final String label;
    private final int minTotalPoints;
    private final int bonusPercent;

    Tier(String label, int minTotalPoints, int bonusPercent) {
        this.label = label;
        this.minTotalPoints = minTotalPoints;
        this.bonusPercent = bonusPercent;
    }

    public String getLabel() {
        return this.label;
    }

    public int getMinTotalPoints() {
        return this.minTotalPoints;
    }

    // extra points in percent that this tier gets on every purchase
    public int getBonusPercent() {
        return this.bonusPercent;
    }

    // the highest tier whose limit the points reach
    public static Tier forTotalPoints(int totalPoints) {
        Tier result = BRONZE;
        for (Tier tier : values()) {
            if (totalPoints >= tier.minTotalPoints) {
                result = tier;
            }
        }
        return result;
    }

    // ordinal() is the position in the list above: BRONZE 0, SILVER 1, GOLD 2
    public boolean isAtLeast(Tier other) {
        return this.ordinal() >= other.ordinal();
    }
}
