package loyalty.exception;

// The customer name is empty.
public class InvalidNameException extends LoyaltyException {
    public InvalidNameException() {
        super("The name cannot be empty");
    }
}
