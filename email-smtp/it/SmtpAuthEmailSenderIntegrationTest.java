import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;

public class SmtpAuthEmailSenderIntegrationTest {

    // TODO: Add earlier tests on missing data in the e-mail and the configuration

    @Test
    public void sendReturnsMessageID() throws Exception {

        final SmtpAuthEmailSender sender = SmtpAuthEmailSender.of(new Properties());

        CompletionStage<String> completionStage = sender.send(msg -> {});
        assertThat(completionStage).isNotNull();
        // TODO: The ID should not be empty
        assertThat(completionStage.toCompletableFuture().get()).isEmpty();
    }
}
