package com.commercetools.sunrise.email;

/**
 * An unchecked exception signalling that an e-mail could not be created or not be modified by a {@link MessageEditor}.
 * <p>
 * Exceptions of this type will often wrap a lower-level exception.
 *
 * @see Exception#getCause()
 */
public class EmailCreationException extends EmailSenderException {

    public EmailCreationException(final String message) {
        super(message);
    }

    public EmailCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EmailCreationException(final Throwable cause) {
        super(cause);
    }

    public EmailCreationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
