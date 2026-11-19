package loyalty.exception;

// The reward number is not in the catalog.
public class RewardNotFoundException extends LoyaltyException {
    public RewardNotFoundException(int number) {
        super("There is no reward with the number " + number);
    }
}
