package io.commercetools.sunrise.email;

/**
 * An unchecked exception signalling that an e-mail could not be created or not be sent due to issues that arise from
 * either the e-mail itself or from the e-mail infrastructure.
 * <p>
 * Exceptions of this type will often wrap a lower-level exception.
 *
 * @see Exception#getCause()
 */
public class EmailSenderException extends RuntimeException {

    public EmailSenderException(final String message) {
        super(message);
    }

    public EmailSenderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EmailSenderException(final Throwable cause) {
        super(cause);
    }

    public EmailSenderException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
