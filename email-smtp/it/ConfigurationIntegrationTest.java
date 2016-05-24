import io.commercetools.sunrise.email.EmailSenderException;
import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigurationIntegrationTest extends AbstractIntegrationTest {

    // Note: It would be desireable to test authentication errors, but
    // Greenmail'S SMTP server does not check credentials.

    @Test
    public void invalidServerHostnameRaisesException() {
        final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(executor,
                "thereIsNoServerByThisName", setup.getPort(),
                SmtpAuthEmailSender.TransportSecurity.None, USERNAME, PASSWORD, TIMEOUT_60_SECONDS);
        assertThatThrownBy(() -> { sender.send(TestUtils.validShortEmail()).toCompletableFuture().join(); })
                .hasCauseInstanceOf(EmailSenderException.class)
                .hasStackTraceContaining("java.net.UnknownHostException: thereIsNoServerByThisName");
    }

    @Test
    public void invalidServerPortRaisesException() {
        final int invalidPort = 123;
        final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(executor,
                setup.getBindAddress(), invalidPort,
                SmtpAuthEmailSender.TransportSecurity.None, USERNAME, PASSWORD, TIMEOUT_60_SECONDS);
        assertThatThrownBy(() -> { sender.send(TestUtils.validShortEmail()).toCompletableFuture().join(); })
                .hasCauseInstanceOf(EmailSenderException.class)
                .hasStackTraceContaining("java.net.ConnectException: Connection refused");
    }

    @Test
    public void portThatDoesNotAcceptConnectionsRaisesException() throws Exception {
        final int timeout300Ms = 300;
        final int openPortOnWhichNoConnectionsAreAccepted = 12346;
        try(ServerSocket serverSocket = new ServerSocket(openPortOnWhichNoConnectionsAreAccepted)) {
            final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(executor,
                    setup.getBindAddress(), openPortOnWhichNoConnectionsAreAccepted,
                    SmtpAuthEmailSender.TransportSecurity.None, USERNAME, PASSWORD, timeout300Ms);
            assertThatThrownBy(() -> { sender.send(TestUtils.validShortEmail()).toCompletableFuture().join(); })
                    .hasCauseInstanceOf(EmailSenderException.class)
                    .hasStackTraceContaining("java.net.SocketTimeoutException: Read timed out");
        }
    }

    @Test
    public void portThatDoesNotSendGreetingRaisesException() throws Exception {
        final int timeout300Ms = 300;
        final int openPort = 12346;
        try(ServerSocket serverSocket = new ServerSocket(openPort)) {
            final Thread acceptor = new Thread(accept1ConnectionButDontWriteGreeting(serverSocket));
            acceptor.start();
            final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(executor,
                    setup.getBindAddress(), openPort,
                    SmtpAuthEmailSender.TransportSecurity.None, USERNAME, PASSWORD, timeout300Ms);
            assertThatThrownBy(() -> { sender.send(TestUtils.validShortEmail()).toCompletableFuture().join(); })
                    .hasCauseInstanceOf(EmailSenderException.class)
                    .hasStackTraceContaining("java.net.SocketTimeoutException: Read timed out");
        }
    }

    private IORunnable accept1ConnectionButDontWriteGreeting(@Nonnull final ServerSocket serverSocket) {
        return () -> { serverSocket.accept(); };
    }

    // Note: it would be desireable to test the write timeout of the Java Mail API, but
    // I cannot think of an easy way to make it write enough data that causes the TCP stack
    // to pause writes, because the Java Mail API likely waits for server acknowledgements
    // before it continues sending data.

    @Test
    public void twoSendersForDifferentPortsDoNotInterfereWithEachOther() throws Exception {
        final int invalidPort = 123;
        final SmtpAuthEmailSender dysfunctionalSender = new SmtpAuthEmailSender(executor,
                setup.getBindAddress(), invalidPort,
                SmtpAuthEmailSender.TransportSecurity.None, USERNAME, PASSWORD, TIMEOUT_60_SECONDS);

        String messageId = sender.send(TestUtils.validShortEmail()).toCompletableFuture().join();

        assertThat(messageId).isNotEmpty();
    }


    /**
     * Used to re-throw {@link IOException}s as unchecked exceptions.
     */
    private interface IORunnable extends Runnable {

        void io() throws IOException;

        @Override
        default void run() {
            try {
                io();
            } catch (IOException e) {
                throw new IllegalStateException("I/O failed", e);
            }
        }
    }

}
