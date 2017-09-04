package com.commercetools.sunrise.email.smtp;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import com.commercetools.sunrise.email.smtp.SmtpConfiguration;
import org.junit.Before;
import org.junit.Rule;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public abstract class AbstractIntegrationTest {

    protected static final String USERNAME = "user";
    protected static final String PASSWORD = "password";
    protected static final int TIMEOUT_60_SECONDS = 60*1000;

    protected static final String CR_LF = "\r\n";
    protected static final String FOO_BAR_AT_DOMAIN_COM = "foo.bar@domain.com";
    protected static final String TEST = "Täst"; // checks encoding, too
    protected static final String HELLO_WORLD = "Hello Wörld!"; // checks encoding, too

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);
    protected Executor executor;
    protected SmtpAuthEmailSender sender;
    protected ServerSetup setup;

    @Before
    public void setup() throws Exception {
        greenMail.setUser(USERNAME, PASSWORD);
        setup = greenMail.getSmtp().getServerSetup();
        executor = new ForkJoinPool(1);
        sender = createSender();
    }

    protected SmtpAuthEmailSender createSender() {
        return createSender(executor);
    }

    protected SmtpAuthEmailSender createSender(Executor executor) {
        final SmtpConfiguration smtpConfiguration = new SmtpConfiguration(setup.getBindAddress(), setup.getPort(),
                SmtpConfiguration.TransportSecurity.None, USERNAME, "foo");
        return new SmtpAuthEmailSender(smtpConfiguration, executor, TIMEOUT_60_SECONDS);
    }

}
