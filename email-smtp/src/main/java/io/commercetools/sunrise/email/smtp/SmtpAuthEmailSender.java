package io.commercetools.sunrise.email.smtp;

import io.commercetools.sunrise.email.EmailSender;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * An e-mail sender that sends e-mail over SMTP using the default implementation of the Java Mail API.
 */
public class SmtpAuthEmailSender implements EmailSender {

    public SmtpAuthEmailSender(@Nonnull final Properties properties) {
        // TODO
    }

    @Override
    @Nonnull
    public CompletionStage<String> send(@Nonnull final Consumer<MimeMessage> messageAuthor) {
        // TODO: Optionally specify the Executor when creating the service
        return CompletableFuture.supplyAsync(() -> "");
    }
}
