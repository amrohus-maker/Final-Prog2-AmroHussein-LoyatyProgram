package loyalty.exception;

// The parent of every error in this program.
// It extends Exception (not RuntimeException), so it is a checked exception:
// the compiler forces us to catch it or to declare it with "throws".
public class LoyaltyException extends Exception {
    public LoyaltyException(String message) {
        super(message);
    }

    public LoyaltyException(String message, Throwable cause) {
        super(message, cause);
    }
}
