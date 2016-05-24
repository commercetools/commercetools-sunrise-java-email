import io.commercetools.sunrise.email.EmailSenderException;
import org.junit.Test;

import javax.mail.Message;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SendingIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void missingRecipientAddressYieldsEmailSenderException() {
        assertThatThrownBy(() -> { sender.send(msg -> {}).toCompletableFuture().join(); })
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(EmailSenderException.class)
                .hasStackTraceContaining("No recipient addresses");
    }

    @Test
    public void theExceptionallyMethodOfCompletionStageCanBeUsedToHandleEmailSenderException() {
        final String cause = sender.send(msg -> {})
                .exceptionally(throwable -> throwable.getCause().getClass().getName())
                .toCompletableFuture().join();
        assertThat(cause).isEqualTo(EmailSenderException.class.getName());
    }

    @Test
    public void missingContentYieldsEmailSenderException() throws Exception {
        final Method thisMethod = SendingIntegrationTest.class
                .getMethod("missingContentYieldsEmailSenderException");
        System.err.println("Note that "+thisMethod.getDeclaringClass().getName()+"."+thisMethod.getName()+"()"
                +" deliberately raises an IllegalStateException / EOFException in the Greenmail implementation"
                +" that you're going to see on System.err in the following.");

        assertThatThrownBy(() -> {
            sender.send(msg -> {
                msg.addRecipients(Message.RecipientType.TO, FOO_BAR_AT_DOMAIN_COM);
            }).toCompletableFuture().join();
        }).hasCauseInstanceOf(EmailSenderException.class)
                .hasStackTraceContaining("No MimeMessage content");
    }

    @Test
    public void missingSubjectYieldsEmailSenderException() {
        assertThatThrownBy(() -> {
            sender.send(msg -> {
                msg.addRecipients(Message.RecipientType.TO, FOO_BAR_AT_DOMAIN_COM);
                msg.setText(HELLO_WORLD, "UTF-8");
            }).toCompletableFuture().join();
        }).hasCauseInstanceOf(EmailSenderException.class)
                .hasStackTraceContaining("451 Requested action aborted: local error in processing");
    }

    @Test
    public void sendReturnsNonEmptyMessageID() throws Exception {
        TestUtils.testSuccessfulSend(greenMail, sender);
    }

    @Test
    public void sendReturnsUniqueMessageID() throws Exception {
        final String messageId1 = sender.send(TestUtils.validShortEmail()).toCompletableFuture().join();
        final String messageId2 = sender.send(TestUtils.validShortEmail()).toCompletableFuture().join();
        assertThat(messageId1).isNotEqualTo(messageId2);
    }

}
