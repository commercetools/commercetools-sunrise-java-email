package com.commercetools.sunrise.email;

/**
 * An unchecked exception signalling that an e-mail could not be sent due to issues that arose from
 * either the e-mail itself or from the e-mail infrastructure.
 * <p>
 * Exceptions of this type will often wrap a lower-level exception.
 *
 * @see Exception#getCause()
 */
public class EmailDeliveryException extends EmailSenderException {

    public EmailDeliveryException(final String message) {
        super(message);
    }

    public EmailDeliveryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EmailDeliveryException(final Throwable cause) {
        super(cause);
    }

    public EmailDeliveryException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
