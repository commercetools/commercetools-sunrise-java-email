package io.commercetools.sunrise.email.smtp;

import io.commercetools.sunrise.email.EmailCreationException;
import io.commercetools.sunrise.email.EmailDeliveryException;
import io.commercetools.sunrise.email.EmailSender;
import io.commercetools.sunrise.email.MessageEditor;

import javax.annotation.Nonnull;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * An e-mail sender that asynchronously sends e-mail via SMTP over TLS using the default implementation of the Java Mail API.
 * <h1>Logging</h1>
 * This class does not log on its own. I relies on logging performed by the Java Mail API instead.
 * The underlying Java Mail API uses {@link java.util.logging.Logger}s. See {@link java.util.logging.LogManager} on how
 * to configure these kinds of loggers, in case you need fine-grained control over logging. In general the log output
 * of the Java Mail API is helpful for development and debugging. For such purposes debug-level log output can be
 * obtained by setting the {@code mail.debug} property to {@code true}. See the
 * <a href="http://www.oracle.com/technetwork/java/faq-135477.html#debug">Java Mail API FAQ</a> for details on how to
 * debug the Java Mail API and other related technologies like SSL that are employed by the Java Mail API.
 * The JavaDoc of the
 * <a href="https://javamail.java.net/nonav/docs/api/index.html?javax/mail/package-summary.html">javax.mail</a>,
 * <a href="https://javamail.java.net/nonav/docs/api/index.html?javax/mail/internet/package-summary.html">javax.mail.internet</a>,
 * <a href="https://javamail.java.net/nonav/docs/api/index.html?com/sun/mail/smtp/package-summary.html">com.sun.mail.smtp</a>,
 * and other packages provide details on log levels.
 */
public class SmtpAuthEmailSender implements EmailSender {

    /**
     * The executor used to send messages asynchronously.
     */
    private final Executor executor;

    /**
     * The session that wraps configuration properties and is used to create messages.
     */
    private final Session session;

    /**
     * Create a new instance using the given executor and configuration. The following example shows how to create an
     * instance of this sender for exploring its functionality. Make sure to read below notes, though.
     * <pre>{@code
     * final String yourHost = ...;
     * final int yourPort = ...;
     * // You may need to choose another transport security available for your server.
     * final SmptConfiguration.TransportSecurity security = SmtpConfiguration.TransportSecurity.STARTTLS;
     * final String username = ...;
     * final String password = ...;
     * final SmtpConfiguration smtpConfiguration = new SmtpConfiguration(yourHost, yourPort, security, username, password);
     * final int threeSeconds = 3*1000;
     * final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(smtpConfiguration, ForkJoinPool.commonPool(), threeSeconds);
     * }</pre>
     * Instances of {@link SmtpAuthEmailSender} might be used as exemplified in {@link EmailSender#send(MessageEditor)}.
     * <h1>Executor and timeouts</h1>
     * The {@link Executor} passed to the constructor enables the sender to send e-mails asynchronously. You may use
     * {@link ForkJoinPool#commonPool()}, which uses a pool of {@code N} threads, where {@code N} is equal to the number
     * of available processors. You may also create a custom {@link ForkJoinPool} instance, or another kind of
     * {@link Executor}. Note that blocked I/O while sending e-mails will block a pool thread. A fork join pool
     * will also queue tasks while all threads are busy or blocked and offers methods like
     * {@link ForkJoinPool#getQueuedTaskCount()} that may be used to monitor the pool at run-time.
     * <p>
     * The constructor also requires a timeout to be specified in milliseconds. If this timeout elapses while the e-mail
     * sender waits to be connected to the SMTP server, waits to read data from the SMTP server, or waits to write data
     * to the SMTP server, an {@link EmailDeliveryException} will be raised and sending of the e-mail that caused the
     * issue will be aborted. If this happens before the e-mail has been received by the SMTP server, the e-mail will
     * not be sent, as users of this service will not typically implement error-handling in such cases. In general,
     * shorter timeouts may lead to more messages being lost in the abscence of further error handling, longer timeouts
     * may lead to more tasks being queued if a {@link ForkJoinPool} is used as {@link Executor}; run-time monitoring of
     * the {@link ForkJoinPool} might help choose a suitable timeout value.
     * <h1>Message size</h1>
     * The e-mail sender does not limit the size of the messages it accepts. Because the {@link #send(MessageEditor)}
     * method fills messages before sending is attempted within the {@link Executor}, it is theoretically possible that
     * a large queue of messages waiting to be send consumes all available memory. Users of this API should therefore
     * ensure that messages do not exceed a certain size.
     * <h1>Configuration correctness</h1>
     * Note that this constructor does not fail fast: the constructor does not create a connection to the mail server to
     * ensure that the connection details and credentials are correct. If you would like to ensure a correct
     * configuration, send a test message at startup and decide on your own what to do if that fails.
     * <h1>Shutdown</h1>
     * If the JVM running this e-mail sender is shut down, the {@link Executor} passed to this constructor should be
     * shut down, too, in a way that ensures that all messages submitted to {@link #send(MessageEditor)} have been sent
     * via SMTP - or a timeout expires in the case too many messages are waiting. If a {@link ForkJoinPool} is used as
     * {@link Executor}, {@link ForkJoinPool#awaitQuiescence(long, TimeUnit)} may be used for this purpose.
     *
     * @param smtpConfiguration how to connect to the SMTP server
     * @param executor          the executor to use, e.g. {@link ForkJoinPool#commonPool()} may be used, but see above
     * @param timeoutMs         the timeout for creating, reading from and writing to SMTP connections in
     *                          milliseconds. To try out {@link SmtpAuthEmailSender} for a low to moderate traffic site
     *                          with an SMTP server on the local network, the choice of the timeout might not be too
     *                          relevant and e.g. 3000ms might fit. But see above on what to consider when choosing a
     *                          timeout for use in a production environment.
     */
    public SmtpAuthEmailSender(@Nonnull final SmtpConfiguration smtpConfiguration, @Nonnull final Executor executor,
                               final int timeoutMs) {
        this.executor = executor;
        final Properties properties = createProperties(smtpConfiguration, timeoutMs);
        properties(properties);
        this.session = createSession(properties, smtpConfiguration);
    }

    /**
     * Creates a new {@link Session} with an {@link Authenticator} that will be used to log into the SMTP server.
     * <p>
     * This method may be overridden to customize session creation. It is invoked by the
     * {@link #SmtpAuthEmailSender(SmtpConfiguration, Executor, int)} constructor.
     *
     * @param properties        the configuration to be applied in the session
     * @param smtpConfiguration how to connect to the SMTP server, incl. the username and password to authenticate with
     * @return the session to obtain messages from
     */
    protected Session createSession(@Nonnull final Properties properties,
                                    @Nonnull final SmtpConfiguration smtpConfiguration) {

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpConfiguration.getUsername(), smtpConfiguration.getPassword());
            }
        });
    }

    /**
     * Creates the properties to configure the {@link Session} that is used to send e-mails.
     *
     * @param smtpConfiguration how to connect to the SMTP server
     * @param timeoutMs         the timeout for creating, reading from and writing to SMTP connections in
     *                          milliseconds, see {@link #SmtpAuthEmailSender(SmtpConfiguration, Executor, int)}
     *                          for details
     * @return the properties to configure the {@link Session} used by this sender
     */
    private Properties createProperties(@Nonnull final SmtpConfiguration smtpConfiguration,
                                        final int timeoutMs) {
        final Properties properties = new Properties();
        if (System.getProperty("mail.debug") != null)
            properties.setProperty("mail.debug", System.getProperty("mail.debug"));
        properties.setProperty("mail.smtp.host", smtpConfiguration.getHost());
        properties.setProperty("mail.smtp.port", "" + smtpConfiguration.getPort());
        properties.setProperty("mail.smtp.auth", "" + true);
        properties.setProperty("mail.smtp.connectiontimeout", "" + timeoutMs);
        properties.setProperty("mail.smtp.timeout", "" + timeoutMs);
        properties.setProperty("mail.smtp.writetimeout", "" + timeoutMs);
        final SmtpConfiguration.TransportSecurity transportSecurity = smtpConfiguration.getTransportSecurity();
        if (transportSecurity == SmtpConfiguration.TransportSecurity.SSL_TLS) {
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            properties.setProperty("mail.smtp.socketFactory.port", "" + smtpConfiguration.getPort());
            properties.setProperty("mail.smtp.ssl.checkserveridentity", "" + true);
        } else if (transportSecurity == SmtpConfiguration.TransportSecurity.STARTTLS) {
            properties.setProperty("mail.smtp.starttls.enable", "" + true);
            properties.setProperty("mail.smtp.starttls.required", "" + true);
            properties.setProperty("mail.smtp.ssl.checkserveridentity", "" + true);
        } else if (transportSecurity == SmtpConfiguration.TransportSecurity.None) {
            // No additional configuration required
        } else {
            throw new IllegalArgumentException("Unknown transport security: " + transportSecurity.name());
        }
        return properties;
    }

    /**
     * This method does nothing by default but may be overridden to customize the configuration of the Java Mail API
     * used by this e-mail sender. The method receives as argument the configuration of the Java Mail API
     * constructed by the invocation of
     * {@link #SmtpAuthEmailSender(SmtpConfiguration, Executor, int)}.
     * <p>
     * Consult the source code of this class before you override this method. The code will help you understand how
     * {@link #SmtpAuthEmailSender(SmtpConfiguration, Executor, int)}
     * creates the properties that are passed to this method.
     * <p>
     * This method may be overridden like in the following example that disables the check of the server identity if
     * {@link SmtpConfiguration.TransportSecurity#SSL_TLS} or {@link SmtpConfiguration.TransportSecurity#STARTTLS} are
     * used for this {@link SmtpAuthEmailSender}. Because the example relaxes security constraints it might only be
     * applied in test setups. Note that properties are always set as strings.
     * <pre>{@code
     * protected void properties(final Properties properties) {
     *       properties.setProperty("mail.smtp.ssl.checkserveridentity", "" + false);
     * } }</pre>
     * <p>
     * The Java Mail API offers many configuration properties. The following packages define relevant properties:
     * <a href="https://javamail.java.net/nonav/docs/api/index.html?javax/mail/package-summary.html">javax.mail</a>,
     * <a href="https://javamail.java.net/nonav/docs/api/index.html?javax/mail/internet/package-summary.html">javax.mail.internet</a>, and
     * <a href="https://javamail.java.net/nonav/docs/api/index.html?com/sun/mail/smtp/package-summary.html">com.sun.mail.smtp</a>.
     *
     * @param properties the properties for the Java Mail API created from the arguments passed to
     *                   {@link #SmtpAuthEmailSender(SmtpConfiguration, Executor, int)}
     */
    protected void properties(@Nonnull final Properties properties) {
    }

    @Override
    @Nonnull
    public CompletionStage<String> send(@Nonnull final MessageEditor messageEditor) {
        final MimeMessage message = createAndFillMessage(messageEditor);
        final CompletableFuture<String> result = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                sendMessage(message);
                result.complete(message.getMessageID());
            } catch (final Throwable t) {//IDE may warn about this, but if fatals are not in the result, it might hang forever
                EmailDeliveryException wrapper = new EmailDeliveryException("Failed to send e-mail", t);
                result.completeExceptionally(wrapper);
            }
        });
        return result;
    }

    /**
     * Creates a new {@link MimeMessage} and passes it to the given {@link MessageEditor} that fills the message.
     * <p>
     * This method may be overridden to customize message creation; it is invoked by {@link #send(MessageEditor)}.
     *
     * @param messageEditor the editor that will be used to fill the empty message created by this method
     * @return the message that is ready for being sent
     * @throws EmailCreationException if there was an error while creating or filling the message
     */
    protected MimeMessage createAndFillMessage(@Nonnull final MessageEditor messageEditor) {
        try {
            MimeMessage message = new MimeMessage(session);
            messageEditor.edit(message);
            return message;
        } catch (Exception e) {
            throw new EmailCreationException("Failed to create e-mail", e);
        }
    }

    /**
     * Sends the given message to the configured SMTP server.
     * <p>
     * This method may be overridden to customize message sending; it is invoked by {@link #send(MessageEditor)}.
     * <p>
     * The implementation of this method uses {@link Transport#send(Message)}, which uses the {@link Session}
     * configuration from the given message and utilizes one SMTP connection per message. This approach avoids tracking
     * connection state.
     *
     * @param message the edited message that is ready for being sent
     * @throws MessagingException may be raised while sending the message. This method does not handle exceptions.
     *                            Exceptions are handled by the invoking {@link #sendMessage(MimeMessage)} method.
     */
    protected void sendMessage(@Nonnull final MimeMessage message) throws MessagingException {
        Transport.send(message);
    }
}
