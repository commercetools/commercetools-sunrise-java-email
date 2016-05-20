package io.commercetools.sunrise.email;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * A service for sending e-mails.
 * <p>Instances of the e-mail service are created with a configuration such that they can later send messages
 * without requiring further credentials or configuration of servers.
 * <p>The service API is based on the Java Mail API. Note that implementations of this service might choose not to
 * <em>send</em> e-mails using the (default implementation of the) Java Mail API but to merely accept messages created
 * using the Java Mail API. You may want to consult the <a href="https://java.net/projects/javamail/pages/Home">samples,
 * JavaDocs and specification of the Java Mail API</a>.
 */
@FunctionalInterface
public interface EmailSender {

    /**
     * Create a completion stage that asynchronously sends an e-mail using the configuration of this e-mail
     * service. Before this method returns, the message author argument of this method is passed an empty
     * {@link MimeMessage} that the {@link EmailSender} created. The message author shall prepare the message
     * so it can be sent. Sending will happen asynchronously, though.
     * <p>
     * Refer to the <a href="https://javamail.java.net/nonav/docs/api/">Java Mail JavaDoc</a> and the
     * <a href="http://javamail.java.net/nonav/docs/JavaMail-1.5.pdf">Java Mail Specification</a> on how to configure
     * instances of {@link MimeMessage}. The following code shows a simple example.
     * <pre>{@code
     * CompletionStage<String> completionStage = emailSender.send(msg -> {
     *      msg.setFrom("foo@domain.com");
     *      msg.setSubject("Subject, "UTF-8");
     *      msg.setText("Text", "UTF-8");
     *      msg.setSentDate(new Date());
     *      msg.setRecipients(Message.RecipientType.TO, "bar@domain.com");
     * });
     * }</pre>
     * The {@link CompletionStage} returned by this method may be combined with further completion stages. To directly
     * send the email and get its message ID, obtain a completable future as in the following example.
     * <pre>{@code
     * String messageID = completionStage.toCompletableFuture().join();
     * }</pre>
     * Exceptions that occur while sending the email are instances of {@link EmailSenderException} that are
     * wrapped in an unchecked {@link CompletionException} thrown by {@link CompletableFuture#join()}.
     * <p>Implementations of this method need to apply timeouts that may be configured when creating the {@link EmailSender}
     * instance. The timeouts avoid denial of service by too many connections waiting for stalled I/O.</p>
     * <p>To send e-mails synchronously, use {@link #sendAndWait(Consumer)}.
     *
     * @param messageAuthor a consumer that fills the empty message created by the email sender.
     *                      Note that {@link MimeMessage} instances are not immutable. Messages passed to the consumer
     *                      must only be used by that consumer instance and must not be passed elsewhere.
     * @return A completion stage for sending the message asynchronously.
     * @see #sendAndWait(Consumer)
     */
    @Nonnull
    CompletionStage<String> send(@Nonnull final Consumer<MimeMessage> messageAuthor);

    /**
     * Send the given message and wait for the result.
     * <p>
     * This method is provided as a convenience method to users who send e-mails synchronously. It is invoked in the
     * same way as {@link #send(Consumer)}, but waits for the result.
     *
     * @param messageAuthor a consumer that fills the empty message created by the email sender.
     *                      Note that {@link MimeMessage} instances are not immutable. Messages passed to the consumer
     *                      must only be used by that consumer instance and must not be passed elsewhere.
     * @return the ID of the successfully sent message (this method will not return otherwise)
     * @throws CompletionException if there was an exception while sending the e-mail. The wrapped exception will be a
     *                             {@link EmailSenderException} if the e-mail could not be sent due to issues with the
     *                             given message or the e-mail infrastructure.
     * @see #send(Consumer)
     */
    @Nonnull
    default String sendAndWait(@Nonnull final Consumer<MimeMessage> messageAuthor) {
        return send(messageAuthor).toCompletableFuture().join();
    }
}
