package io.commercetools.sunrise.email.smtp;

import io.commercetools.sunrise.email.EmailSender;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletionStage;

/**
 * An e-mail sender that sends e-mail over SMTP using the default implementation of the Java Mail API.
 */
public class SmtpAuthEmailSender implements EmailSender {

    @Override
    public
    @Nonnull
    CompletionStage<String> send(@Nonnull final MimeMessage message) {
        throw new IllegalStateException("Not yet implemented");
    }
}
