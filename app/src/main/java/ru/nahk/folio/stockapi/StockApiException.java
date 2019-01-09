package ru.nahk.folio.stockapi;

/**
 * Exception that is thrown by {@link StockApi} implementations.
 */
public class StockApiException extends Exception {
    /**
     * Constructs an {@link StockApiException} with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     */
    StockApiException(String message) {
        super(message);
    }

    /**
     * Constructs an {@link StockApiException} with the specified detail message
     * and cause.
     *
     * <p> Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     */
    StockApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
