package com.commercetools.sunrise.email;

/**
 * An abstract base class for unchecked exception signalling issues related to an {@link EmailSender}. See the
 * sub-classes for specific exceptions.
 * <p>
 * Exceptions of this type will often wrap a lower-level exception.
 *
 * @see Exception#getCause()
 */
public abstract class EmailSenderException extends RuntimeException {

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
