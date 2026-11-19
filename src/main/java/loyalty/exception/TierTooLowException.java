package loyalty.exception;

// The reward is only for a higher tier. We keep the tier as text so that
// this package does not need to know the domain package.
public class TierTooLowException extends LoyaltyException {
    private final String tierName;

    public TierTooLowException(String tierName) {
        super("This reward needs the " + tierName + " tier or higher");
        this.tierName = tierName;
    }

    public String getTierName() {
        return this.tierName;
    }
}
