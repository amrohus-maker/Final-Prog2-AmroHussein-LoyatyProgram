package loyalty.exception;

// There is no customer with the id or email that was typed.
public class CustomerNotFoundException extends LoyaltyException {
    public CustomerNotFoundException(String what) {
        super("No customer found for " + what);
    }
}
