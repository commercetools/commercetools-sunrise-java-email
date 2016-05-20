import io.commercetools.sunrise.email.EmailSender;
import org.junit.Test;

import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailSenderTest {

    public static final String ID = "ID";

    @Test
    public void sendAndWaitDefaultMethodDelegatesToSend() throws Exception {
        final EmailSender sender = (a) -> CompletableFuture.completedFuture(ID);
        final Consumer<MimeMessage> author = (msg) -> {};
        assertThat(sender.sendAndWait(author)).isSameAs(ID);
    }

}
