package com.commercetools.sunrise.email.smtp;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

import static com.commercetools.sunrise.email.smtp.SmtpConfiguration.TransportSecurity.STARTTLS;

/**
 * An e-mail sender pre-configured for Gmail servers.
 */
public class GmailSmtpEmailSender extends SmtpAuthEmailSender {

    /**
     * Create an e-mail sender that connects to smtp.gmail.com:587 using (and requiring)
     * {@link SmtpConfiguration.TransportSecurity#STARTTLS} security.
     * <p>
     * See {@link SmtpAuthEmailSender#SmtpAuthEmailSender(SmtpConfiguration, Executor, int)}
     * for details on how to configure this service.
     *
     * @param username  the complete Gmail e-mail address
     * @param password  the Gmail password
     * @param executor  the executor to use, e.g. {@link java.util.concurrent.ForkJoinPool#commonPool()} may be used,
     *                  but see {@link SmtpAuthEmailSender#SmtpAuthEmailSender(SmtpConfiguration, Executor, int)}
     *                  for details
     * @param timeoutMs the timeout for creating, reading from and writing to SMTP connections in
     *                  milliseconds. To try out {@link GmailSmtpEmailSender} for a low to moderate traffic site,
     *                  10*1000ms might fit. But see
     *                  {@link SmtpAuthEmailSender#SmtpAuthEmailSender(SmtpConfiguration, Executor, int)} on what to
     *                  consider when choosing a timeout for use in a production environment.
     */
    public GmailSmtpEmailSender(@Nonnull final String username, @Nonnull final String password, @Nonnull final Executor executor,
                                final int timeoutMs) {
        // Configuration is based on https://support.google.com/a/answer/176600?hl=en
        super(new SmtpConfiguration("smtp.gmail.com", 587, STARTTLS, username, password), executor, timeoutMs);
    }

}
