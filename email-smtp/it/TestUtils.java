import com.icegreen.greenmail.junit.GreenMailRule;
import io.commercetools.sunrise.email.MessageEditor;
import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectArrayAssert;

import javax.annotation.Nonnull;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    private static final String UTF_8 = "UTF-8";

    public static void testSuccessfulSend(@Nonnull final GreenMailRule greenMail,
                                          @Nonnull final SmtpAuthEmailSender sender) throws Exception {
        final String messageId = sender.send(validShortEmail()).toCompletableFuture().join();
        final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        Assertions.assertThat(messageId).isNotEmpty();
        Assertions.assertThat(receivedMessages).hasSize(1);

        final MimeMessage message = receivedMessages[0];
        Assertions.assertThat(message.getSubject()).isEqualTo(AbstractIntegrationTest.TEST);
        Assertions.assertThat(message.getAllRecipients()).hasSize(1);
        Assertions.assertThat(message.getAllRecipients()[0]).isEqualTo(new InternetAddress(AbstractIntegrationTest.FOO_BAR_AT_DOMAIN_COM));
        Assertions.assertThat(message.getContent()).isEqualTo(AbstractIntegrationTest.HELLO_WORLD + AbstractIntegrationTest.CR_LF);
    }

    public static MessageEditor validShortEmail() {
        return msg -> {
            // Automatically creates a FROM address based on javax.mail.internet.InternetAddress.getLocalAddress(Session)
            msg.addRecipients(Message.RecipientType.TO, AbstractIntegrationTest.FOO_BAR_AT_DOMAIN_COM);
            msg.setSubject(AbstractIntegrationTest.TEST, UTF_8);
            msg.setText(AbstractIntegrationTest.HELLO_WORLD, UTF_8);
        };
    }


    public static MessageEditor getEditorWith300MsDelay(final int index) {
        return msg -> {
            synchronized (TestUtils.class) {
                TestUtils.class.wait(300);
                getEditor(index).edit(msg);
            }
        };
    }

    public static void sendMessages(@Nonnull final GreenMailRule greenMail,
                                    @Nonnull final SmtpAuthEmailSender sender,
                                    @Nonnull final Consumer<SmtpAuthEmailSender> sendingProcess,
                                    @Nonnull final Consumer<ObjectArrayAssert<Object>> evaluator) {
        sendingProcess.accept(sender);
        evaluator.accept(assertThat(greenMail.getReceivedMessages()).extracting("subject"));
    }

    public static CompletionStage sendMessages(@Nonnull final SmtpAuthEmailSender sender,
                                               final int offsetToAddMessageIndexTo,
                                               final int numberOfMessagesToSend) {
        CompletionStage lastCompletionStage = null;
        for (int i = 1; i <= numberOfMessagesToSend; i++) {
            lastCompletionStage = sender.send(getEditor(offsetToAddMessageIndexTo + i));
        }
        return lastCompletionStage;
    }

    public static MessageEditor getEditor(final int index) {
        return msg -> {
            msg.setRecipients(Message.RecipientType.TO, "foo" + index + "@domain.com");
            msg.setSubject("Message " + index, UTF_8);
            msg.setText("Content " + index, UTF_8);
        };
    }
}