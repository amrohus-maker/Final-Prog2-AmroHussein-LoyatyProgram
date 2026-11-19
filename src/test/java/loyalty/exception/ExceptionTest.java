package loyalty.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionTest {
    @Test
    void everyErrorIsALoyaltyExceptionWithAMessage() {
        // all ten classes can be put in one list of the parent type
        List<LoyaltyException> errors = new ArrayList<>();
        errors.add(new InvalidNameException());
        errors.add(new InvalidEmailException("abc"));
        errors.add(new InvalidAmountException());
        errors.add(new InvalidChoiceException("x"));
        errors.add(new DuplicateEmailException("a@b.com"));
        errors.add(new CustomerNotFoundException("id 5"));
        errors.add(new RewardNotFoundException(9));
        errors.add(new InsufficientPointsException(100, 40));
        errors.add(new TierTooLowException("Gold"));
        errors.add(new StorageException("could not read the file"));

        assertEquals(10, errors.size());
        for (LoyaltyException error : errors) {
            assertNotNull(error.getMessage());
            assertFalse(error.getMessage().trim().isEmpty());
        }
    }

    @Test
    void oneCatchHandlesEveryKindOfError() {
        String shown = "";
        try {
            throw new DuplicateEmailException("a@b.com");
        } catch (LoyaltyException e) {
            shown = e.getMessage();
        }
        assertTrue(shown.contains("a@b.com"));
    }

    @Test
    void messagesSayWhatWentWrong() {
        assertEquals("The name cannot be empty", new InvalidNameException().getMessage());
        assertEquals("The amount must be above 0 and not more than 100000", new InvalidAmountException().getMessage());
        assertTrue(new InvalidEmailException("abc").getMessage().contains("abc"));
        assertTrue(new CustomerNotFoundException("id 5").getMessage().contains("id 5"));
        assertTrue(new RewardNotFoundException(9).getMessage().contains("9"));
        assertTrue(new InvalidChoiceException("x").getMessage().contains("x"));
    }

    @Test
    void insufficientPointsKnowsTheNumbers() {
        InsufficientPointsException e = new InsufficientPointsException(100, 40);
        assertEquals(100, e.getNeeded());
        assertEquals(40, e.getAvailable());
        assertEquals(60, e.getMissing());
        assertEquals("Needs 100 points but only 40 are available", e.getMessage());
    }

    @Test
    void tierTooLowNamesTheTier() {
        TierTooLowException e = new TierTooLowException("Gold");
        assertEquals("Gold", e.getTierName());
        assertTrue(e.getMessage().contains("Gold"));
    }

    @Test
    void storageExceptionKeepsTheOriginalCause() {
        IOException cause = new IOException("disk is full");
        StorageException e = new StorageException("could not write the file", cause);
        assertSame(cause, e.getCause());
    }
}
