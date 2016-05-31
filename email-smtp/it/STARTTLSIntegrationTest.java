import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.commercetools.sunrise.email.EmailDeliveryException;
import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import io.commercetools.sunrise.email.smtp.SmtpConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class STARTTLSIntegrationTest {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);
    private Executor executor;
    private SmtpAuthEmailSender sender;

    @Before
    public void setup() throws Exception {
        executor = runnable -> runnable.run();
        sender = createSender();
    }

    private SmtpAuthEmailSender createSender() {
        final ServerSetup setup = greenMail.getSmtp().getServerSetup();
        final String username = "user";
        final String password = "password";
        final int timeout60Seconds = 60*1000;
        greenMail.setUser(username, password);
        final SmtpConfiguration smtpConfiguration = new SmtpConfiguration(setup.getBindAddress(), setup.getPort(),
                SmtpConfiguration.TransportSecurity.STARTTLS, username, password);
        return new SmtpAuthEmailSender(smtpConfiguration, executor,
                timeout60Seconds);
    }

    @Test
    public void abortConnectionIfServerDoesNotSupportSTARTTLS() throws Exception {
        // The Greenmail server used for integration-testing does not support STARTTLS.
        assertThatThrownBy(() -> { TestUtils.testSuccessfulSend(greenMail, sender); })
                .hasCauseInstanceOf(EmailDeliveryException.class)
                .hasStackTraceContaining("STARTTLS is required but host does not support STARTTLS");
    }
}
