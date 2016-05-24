import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender.TransportSecurity;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

public class SmtpAuthEmailSenderTest {

    @Test
    public void theSuppliedExecutorIsUsed() throws Exception {
        final CheckingExecutor executor = new CheckingExecutor();
        final int timeout60Seconds = 60*1000;
        final SmtpAuthEmailSender sender = new SmtpAuthEmailSender(executor, "host", 90000,
                TransportSecurity.None, "user", "password", timeout60Seconds);

        sender.send(msg -> { });

        assertThat(executor.hasBeenUsed).isTrue();
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
