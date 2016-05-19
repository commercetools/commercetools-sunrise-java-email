import io.commercetools.sunrise.email.EmailSender;
import io.commercetools.sunrise.email.MessageAuthor;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmailSenderTest {

    public static final String ID = "ID";

    @Test
    public void sendAndWaitDefaultMethodDelegatesToSend() throws Exception {
        final MessageAuthor author = mock(MessageAuthor.class);

        final int timeout = 1000;
        final TimeUnit timeUnit = TimeUnit.SECONDS;
        final CompletableFuture completableFuture = mock(CompletableFuture.class);
        when(completableFuture.get(timeout, timeUnit)).thenReturn(ID);

        final CompletionStage completionStage = mock(CompletionStage.class);
        when(completionStage.toCompletableFuture()).thenReturn(completableFuture);

        final EmailSender sender = new EmailSender() {
            @Nonnull
            @Override
            public CompletionStage<String> send(@Nonnull final MessageAuthor author) {
                return completionStage;
            }
        };

        assertThat(sender.sendAndWait(author, timeout, timeUnit)).isSameAs(ID);
    }

}
