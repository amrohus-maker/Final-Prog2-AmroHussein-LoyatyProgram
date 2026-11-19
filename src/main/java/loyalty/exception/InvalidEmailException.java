package loyalty.exception;

// The email has no @ or no dot in the right place.
public class InvalidEmailException extends LoyaltyException {
    public InvalidEmailException(String text) {
        super("That does not look like an email address: " + text);
    }
}
