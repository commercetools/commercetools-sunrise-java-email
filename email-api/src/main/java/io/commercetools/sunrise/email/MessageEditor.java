package io.commercetools.sunrise.email;

import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * A functional interface for modifying a message without needing to handle checked exceptions like the
 * {@link MessagingException}s thrown by {@link MimeMessage}.
 * <p>
 * This interface will typically be implemented by lambda expressions passed to
 * {@link EmailSender#send(MessageEditor)}. See the JavaDoc of that method for an example.
 */
@FunctionalInterface
public interface MessageEditor {

    /**
     * Implementations of this method modify the given message. See the JavaDoc of
     * {@link EmailSender#send(MessageEditor)} for an example of a lambda expression that implements this interface.
     *
     * @param message the message to modify
     * @throws Exception if modification fails
     */
    MimeMessage edit(@Nonnull final MimeMessage message) throws Exception;
}
