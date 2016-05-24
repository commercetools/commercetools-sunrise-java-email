package io.commercetools.sunrise.email;

import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * A functional interface for modifying a message without needing to handle checked exceptions like the
 * {@link MessagingException}s thrown by {@link MimeMessage}.
 */
@FunctionalInterface
public interface MessageEditor {

    /**
     * Implementations of this method modify the given message.
     *
     * @param message the message to modify
     * @throws MessagingException if modification fails
     */
    void edit(@Nonnull final MimeMessage message) throws Exception;
}
