package io.commercetools.sunrise.email.smtp;

import io.commercetools.sunrise.email.EmailSender;
import io.commercetools.sunrise.email.EmailSenderException;
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
     * How to secure the connection to the SMTP server.
     */
    public enum TransportSecurity {

        /**
         * This mode shall only be used for automatic tests. A plain-text connection will be used to communicate to the
         * server.
         */
        None,

        /**
         * The connection will be established using SSL/TLS right from the start. The service needs to be configured to
         * connect to the dedicated port at which the server listens for SSL/TLS connections.
         */
        SSL_TLS,

        /**
         * A plain-text connection will be established and be switched to TLS. SMTP servers supporting this mode listen
         * on a single port only instead of opening a second secured port like in the case of {@link #SSL_TLS}.
         * The {@link SmtpAuthEmailSender} instance using this transport security will require the switch to TLS and
         * will not continue if the plain-text connection cannot be switched to TLS.
         */
        STARTTLS

    }

    /**
     * The executor used to send messages asynchronously.
     */
    private final Executor executor;

    /**
     * The session that wraps configuration properties and is used to create messages.
     */
    private final Session session;

    /**
     * Create a new instance using the given executor and configuration.
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
     * to the SMTP server, an {@link EmailSenderException} will be raised and sending of the e-mail that caused the
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
     * @param executor          the executor to use, e.g. {@link ForkJoinPool#commonPool()} may be used, but see above
     * @param host              the name of the host running the SMTP server to use
     * @param port              the port of the SMTP server to use
     * @param transportSecurity how to secure the SMTP connection
     * @param username          the username to use for authenticating with the SMTP server
     * @param password          the password to authenticate with
     * @param timeoutMs         the timeout for creating, reading from and writing to SMTP connections in
     *                          milliseconds, see above for details
     */
    public SmtpAuthEmailSender(@Nonnull final Executor executor, @Nonnull final String host, final int port,
                               @Nonnull final TransportSecurity transportSecurity,
                               @Nonnull final String username, @Nonnull final String password,
                               final int timeoutMs) {
        this.executor = executor;
        final Properties properties = createProperties(host, port, transportSecurity, timeoutMs);
        this.session = createSession(properties, username, password);
    }

    /**
     * Creates a new {@link Session} with an {@link Authenticator} that will be used to log into the SMTP server.
     * <p>
     * This method may be overridden to customize session creation. It is invoked by the
     * {@link #SmtpAuthEmailSender(Executor, String, int, TransportSecurity, String, String, int)} constructor.
     *
     * @param properties the configuration to be applied in the session
     * @param username   the username to log into the SMTP server
     * @param password   the password to log into the SMTP server
     * @return the session to obtain messages from
     */
    protected Session createSession(@Nonnull final Properties properties,
                                    @Nonnull final String username, @Nonnull final String password) {

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Creates the properties to configure the {@link Session} that is used to send e-mails.
     * <p>
     * This method may be overridden to customize the configuration of the Java Mail API used by this e-mail sender.
     * This method is invoked by the
     * {@link #SmtpAuthEmailSender(Executor, String, int, TransportSecurity, String, String, int)} constructor.
     * <p>
     * Consult the source code of this method before you override it. You may either replace its implementation or
     * invoke it in the overriding implementation and adjust the properties set by this method.
     * <p>
     * The Java Mail API offers many configuration properties. The following packages define relevant properties:
     * <a href="https://javamail.java.net/nonav/docs/api/index.html?javax/mail/package-summary.html">javax.mail</a>,
     * <a href="https://javamail.java.net/nonav/docs/api/index.html?javax/mail/internet/package-summary.html">javax.mail.internet</a>, and
     * <a href="https://javamail.java.net/nonav/docs/api/index.html?com/sun/mail/smtp/package-summary.html">com.sun.mail.smtp</a>.
     *
     * @param host              the name of the host at which the SMTP server is available
     * @param port              the port number at which the SMTP server is listening for incoming connections
     * @param transportSecurity how to secure the connection to the SMTP server
     * @param timeoutMs         the timeout for creating, reading from and writing to SMTP connections in
     *                          milliseconds, see {@link #SmtpAuthEmailSender(Executor, String, int, TransportSecurity, String, String, int)}
     *                          for details
     * @return the properties to configure the {@link Session} used by this sender
     */
    protected Properties createProperties(@Nonnull final String host, final int port,
                                          @Nonnull final TransportSecurity transportSecurity,
                                          final int timeoutMs) {
        final Properties properties = new Properties();
        if (System.getProperty("mail.debug") != null)
            properties.setProperty("mail.debug", System.getProperty("mail.debug"));
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", "" + port);
        properties.setProperty("mail.smtp.auth", "" + true);
        properties.setProperty("mail.smtp.connectiontimeout", "" + timeoutMs);
        properties.setProperty("mail.smtp.timeout", "" + timeoutMs);
        properties.setProperty("mail.smtp.writetimeout", "" + timeoutMs);
        if (transportSecurity == TransportSecurity.SSL_TLS) {
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            properties.setProperty("mail.smtp.socketFactory.port", "" + port);
            properties.setProperty("mail.smtp.ssl.checkserveridentity", "" + true);
        } else if (transportSecurity == TransportSecurity.STARTTLS) {
            properties.setProperty("mail.smtp.starttls.enable", "" + true);
            properties.setProperty("mail.smtp.starttls.required", "" + true);
            properties.setProperty("mail.smtp.ssl.checkserveridentity", "" + true);
        } else if (transportSecurity == TransportSecurity.None) {
            // No additional configuration required
        } else {
            throw new IllegalArgumentException("Unknown transport security: " + transportSecurity.name());
        }
        return properties;
    }

    @Override
    @Nonnull
    public CompletionStage<String> send(@Nonnull final MessageEditor messageEditor) {
        final MimeMessage message = createAndFillMessage(messageEditor);
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendMessage(message);
                return message.getMessageID();
            } catch (Exception e) {
                throw new EmailSenderException("Failed to send e-mail", e);
            }
        }, executor);
    }

    /**
     * Creates a new {@link MimeMessage} and passes it to the given {@link MessageEditor} that fills the message.
     * <p>
     * This method may be overridden to customize message creation; it is invoked by {@link #send(MessageEditor)}.
     *
     * @param messageEditor the editor that will be used to fill the empty message created by this method
     * @return the message that is ready for being sent
     * @throws EmailSenderException if there was an error while creating or filling the message
     */
    protected MimeMessage createAndFillMessage(@Nonnull final MessageEditor messageEditor) {
        try {
            MimeMessage message = new MimeMessage(session);
            messageEditor.edit(message);
            return message;
        } catch (Exception e) {
            throw new EmailSenderException("Failed to create e-mail", e);
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
