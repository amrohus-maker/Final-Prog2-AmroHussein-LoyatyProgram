package loyalty.exception;

// A customer with this email is already registered.
public class DuplicateEmailException extends LoyaltyException {
    public DuplicateEmailException(String email) {
        super("That email is already registered: " + email);
    }
}
