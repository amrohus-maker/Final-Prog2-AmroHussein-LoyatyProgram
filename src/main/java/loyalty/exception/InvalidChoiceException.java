package loyalty.exception;

// The user typed something in the menu that is not one of the choices.
public class InvalidChoiceException extends LoyaltyException {
    public InvalidChoiceException(String text) {
        super("That is not a choice in the menu: " + text);
    }
}
