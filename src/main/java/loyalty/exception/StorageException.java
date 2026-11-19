package loyalty.exception;

// The data file could not be read or written, or its content is damaged.
public class StorageException extends LoyaltyException {
    public StorageException(String message) {
        super(message);
    }

    // keeps the original error (for example an IOException) as the cause
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
