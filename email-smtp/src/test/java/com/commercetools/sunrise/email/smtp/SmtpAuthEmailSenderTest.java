package com.commercetools.sunrise.email.smtp;

import com.commercetools.sunrise.email.EmailCreationException;
import com.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import com.commercetools.sunrise.email.smtp.SmtpConfiguration;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SmtpAuthEmailSenderTest {

    private static final int TIMEOUT_60_SECONDS = 60 * 1000;
    private static final SmtpConfiguration DUMMY_CONFIGURATION = new SmtpConfiguration("host", 90000,
            SmtpConfiguration.TransportSecurity.None, "user", "password");

    @Test
    public void theSuppliedExecutorIsUsed() throws Exception {
        final CheckingExecutor executor = new CheckingExecutor();
        final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(DUMMY_CONFIGURATION, executor, TIMEOUT_60_SECONDS);

        sender.send(msg -> { });

        assertThat(executor.hasBeenUsed).isTrue();
    }

    @Test
    public void exceptionsInTheMessagEditorRaiseAnEmailCreationException() {
        final Executor executor = runnable -> runnable.run();
        final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(DUMMY_CONFIGURATION, executor, TIMEOUT_60_SECONDS);
        assertThatThrownBy(() -> {
            sender.send(msg -> {
                    throw new IllegalStateException("Creation fails");
                });
        }).isInstanceOf(EmailCreationException.class)
          .hasCauseInstanceOf(IllegalStateException.class)
          .hasStackTraceContaining("Creation fails");
    }

    private static class CheckingExecutor implements Executor {
        private boolean hasBeenUsed = false;

        @Override
        public void execute(final Runnable command) {
            hasBeenUsed = true;
            command.run();
        }
    }
}
