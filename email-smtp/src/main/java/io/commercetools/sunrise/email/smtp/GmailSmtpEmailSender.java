package io.commercetools.sunrise.email.smtp;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

import static io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender.TransportSecurity.STARTTLS;

/**
 * An e-mail sender pre-configured for Gmail servers.
 */
public class GmailSmtpEmailSender extends SmtpAuthEmailSender {

    /**
     * Create an e-mail sender that connects to smtp.gmail.com:587 using (and requiring)
     * {@link io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender.TransportSecurity#STARTTLS} security.
     * <p>
     * See {@link SmtpAuthEmailSender#SmtpAuthEmailSender(Executor, String, int, TransportSecurity, String, String, int)}
     * for details on how to configure this service.
     *
     * @param executor  the executor to use, e.g. {@link java.util.concurrent.ForkJoinPool#commonPool()} may be used,
     *                  but see {@link SmtpAuthEmailSender#SmtpAuthEmailSender(Executor, String, int, TransportSecurity, String, String, int)}
     *                  for details
     * @param username  the complete Gmail e-mail address
     * @param password  the Gmail password
     * @param timeoutMs the timeout for creating, reading from and writing to SMTP connections in
     *                  milliseconds, see {@link SmtpAuthEmailSender#SmtpAuthEmailSender(Executor, String, int, TransportSecurity, String, String, int)}
     *                  for details
     */
    public GmailSmtpEmailSender(@Nonnull final Executor executor,
                                @Nonnull final String username, @Nonnull final String password,
                                final int timeoutMs) {
        // Configuration is based on https://support.google.com/a/answer/176600?hl=en
        super(executor, "smtp.gmail.com", 587, STARTTLS, username, password, timeoutMs);
    }

}
