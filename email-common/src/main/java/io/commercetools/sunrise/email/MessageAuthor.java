package io.commercetools.sunrise.email;

import javax.mail.internet.MimeMessage;
import java.util.function.Consumer;

/**
 * A functional interface for filling empty messages that have been created by an {@link EmailSender} so they can be
 * send.
 *
 * @see EmailSender#send(MessageAuthor)
 */
@FunctionalInterface
public interface MessageAuthor extends Consumer<MimeMessage> {
}
