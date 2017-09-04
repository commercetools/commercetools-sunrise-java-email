package com.commercetools.sunrise.email.smtp;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.DummySSLServerSocketFactory;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.security.Security;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Note that the test in this class is disabled by default because the dummy factories cannot be loaded in sbt. The most
 * likely cause is that Java attempts to load the socket factories using the system classloader whereas sbt adds the test
 * classes to another classloader.
 */
public class SSLIntegrationTest {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTPS);
    private Executor executor;
    private SmtpAuthEmailSender sender;

    @Before
    public void setup() throws Exception {
        installDummySSLSocketFactoriesThatAreRequiredToValidateTheSelfSignedCertificateOfGreenmail();
        executor = runnable -> runnable.run();
        sender = createSender();
    }

    private SmtpAuthEmailSender createSender() {
        final ServerSetup setup = greenMail.getSmtps().getServerSetup();
        final String username = "user";
        final String password = "password";
        final int timeout60Seconds = 60*1000;
        greenMail.setUser(username, password);
        final SmtpConfiguration smtpConfiguration = new SmtpConfiguration(setup.getBindAddress(), setup.getPort(),
                SmtpConfiguration.TransportSecurity.SSL_TLS, username, password);
        return new SmtpAuthEmailSenderThatDoesNotCheckServerIdentity(smtpConfiguration, executor,  timeout60Seconds);
    }

    private void installDummySSLSocketFactoriesThatAreRequiredToValidateTheSelfSignedCertificateOfGreenmail() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory.class.getName());
        Security.setProperty("ssl.ServerSocketFactory.provider", DummySSLServerSocketFactory.class.getName());
    }

    // Remove the @Ignore annotation to run this test locally in an IDE.
    // See the class JavaDoc for why this test is ignored.
    @Ignore
    @Test
    public void sendReturnsNonEmptyMessageID() throws Exception {
        TestUtils.testSuccessfulSend(greenMail, sender);
    }

    private static class SmtpAuthEmailSenderThatDoesNotCheckServerIdentity extends SmtpAuthEmailSender {

        public SmtpAuthEmailSenderThatDoesNotCheckServerIdentity(@Nonnull final SmtpConfiguration smtpConfiguration,
                                                                 @Nonnull final Executor executor, final int timeoutMs) {
            super(smtpConfiguration, executor, timeoutMs);
        }

        @Override
        protected void properties(@Nonnull final Properties properties) {
            // The identity of the SMTP server running on the local host cannot be determined
            // with the self-signed certificate, hence the test disables the default check of the
            // server identity.
            properties.setProperty("mail.smtp.ssl.checkserveridentity", "" + false);
        }
    }
}
