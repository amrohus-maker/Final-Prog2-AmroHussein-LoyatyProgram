package loyalty.exception;

// The purchase amount is not above 0, or it is far too big.
public class InvalidAmountException extends LoyaltyException {
    public InvalidAmountException() {
        super("The amount must be above 0 and not more than 100000");
    }
}
