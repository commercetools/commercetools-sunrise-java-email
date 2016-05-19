package io.commercetools.sunrise.email.smtp;

import io.commercetools.sunrise.email.EmailSender;
import io.commercetools.sunrise.email.MessageAuthor;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * An e-mail sender that sends e-mail over SMTP using the default implementation of the Java Mail API.
 */
public class SmtpAuthEmailSender implements EmailSender {

    protected SmtpAuthEmailSender(@Nonnull final Properties properties) {
    }

    public static
    @Nonnull
    SmtpAuthEmailSender of(@Nonnull final Properties properties) {
        return new SmtpAuthEmailSender(properties);
    }

    @Override
    public
    @Nonnull
    CompletionStage<String> send(@Nonnull final MessageAuthor author) {
        return CompletableFuture.supplyAsync(() -> "");
    }
}
