package io.commercetools.sunrise.email;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.*;

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
     * Create a completion stage that lazily sends the given {@link MimeMessage} using the configuration of this e-mail
     * service.
     * <p>Refer to the <a href="https://javamail.java.net/nonav/docs/api/">Java Mail JavaDoc</a> and the
     * <a href="http://javamail.java.net/nonav/docs/JavaMail-1.5.pdf">Java Mail Specification</a> on how to create
     * and configure instances of {@link MimeMessage}. The following code shows a simple example.
     * <pre>{@code
     * MimeMessage message = new MimeMessage();
     * msg.setFrom("foo@domain.com");
     * msg.setSubject("Subject, "UTF-8");
     * msg.setText("Text", "UTF-8");
     * msg.setSentDate(new Date());
     * msg.setRecipients(Message.RecipientType.TO, "bar@domain.com");
     * }</pre>
     * This method returns a {@link CompletionStage} that may be combined with further completion stages. To
     * send the email and obtain its message ID, obtain a completable future as in the following example.
     * <pre>{@code
     * CompletionStage<String> completionStage = emailSender.send(message);
     * String messageID = completionStage.toCompletableFuture().get();
     * }</pre>
     * Checked exceptions that occur while sending the email are instances of {@link EmailSenderException} that are
     * wrapped in an {@link ExecutionException} thrown by {@link CompletableFuture#get()}.
     * <p>To send e-mails synchronously, use {@link #sendAndWait(MimeMessage, int, TimeUnit)}.
     *
     * @param message the message to be sent. Note that {@link MimeMessage} instances are not immutable. Because the
     *                EmailSender service may alter a message passed to it concurrently, users of EmailSender need to
     *                ensure that messages passed to the EmailSender are not referenced anywhere else and are not re-used.
     * @return A completion stage for lazily sending the message.
     * @see #sendAndWait(MimeMessage, int, TimeUnit)
     */
    @Nonnull
    CompletionStage<String> send(@Nonnull final MimeMessage message);

    /**
     * Send the given message and wait for the result.
     * <p>
     * This method is provided as a convenience method to users who send e-mails synchronously. It is invoked in the
     * same way as {@link #send(MimeMessage)}, but with an additional timeout. Note that the choice of the
     * timeout influences how your application performs under heavy load and in the case of failure of the e-mail
     * infrastructure.
     *
     * @param message the message to be sent. Note that {@link MimeMessage} instances are not immutable. Because the
     *                EmailSender service may alter a message passed to it concurrently, users of EmailSender need to
     *                ensure that messages passed to the EmailSender are not referenced anywhere else and are not re-used.
     * @param timeout the amount of time to wait until a {@link TimeoutException} is raised
     * @param unit    the unit of the time amount
     * @return the ID of the successfully sent message (this method will not return otherwise)
     * @throws ExecutionException   if there was an exception while sending the e-mail. The wrapped exception will be a
     *                              {@link EmailSenderException} if the e-mail could not be sent due to issues with the
     *                              given message or the e-mail infrastructure.
     * @throws TimeoutException     if the timeout elapsed
     * @throws InterruptedException if the current thread was interrupted while waiting for the e-mail to be sent
     * @see #send(MimeMessage)
     */
    default
    @Nonnull
    String sendAndWait(@Nonnull final MimeMessage message, final int timeout, @Nonnull final TimeUnit unit)
            throws ExecutionException, TimeoutException, InterruptedException {
        return send(message).toCompletableFuture().get(timeout, unit);
    }
}
