import io.commercetools.sunrise.email.MessageEditor;
import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AsynchronousIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void editorIsFinishedBeforeSendReturns() {
        final SlowButFinishingMessageEditor editor = new SlowButFinishingMessageEditor();
        final CompletionStage<String> completionStage = sender.send(editor);
        final boolean editorFinished = editor.finished;
        // Wait for sending to complete so the test is done when this method exits
        // and does not interfere with other tests.
        completionStage.toCompletableFuture().join();
        assertThat(editorFinished).isTrue();
    }

    @Test
    public void sendTwoMessagesAndWaitForBoth() {
        sender.send(TestUtils.getEditor(1)).thenAcceptBoth(
                sender.send(TestUtils.getEditor(2)),
                (id1, id2) -> {
                    assertThat(greenMail.getReceivedMessages())
                            .extracting("messageID")
                            .containsExactlyInAnyOrder(id1, id2);
                }).toCompletableFuture().join();
    }

    @Test
    public void sendTwoMessagesAsynchronouslyWith1PooledThreadSoThatTheBlockingMessageIsDeliveredFirst() throws Exception {
        final ForkJoinPool pool = new ForkJoinPool(1);
        final SmtpAuthEmailSender sender = createSender(pool);

        sender.send(TestUtils.getEditorWith300MsDelay(1)).thenAcceptBoth(
                sender.send(TestUtils.getEditor(2)),
                (id1, id2) -> {
                    assertThat(greenMail.getReceivedMessages())
                            .extracting("subject")
                            .containsExactly("Message 1", "Message 2");
                }).toCompletableFuture().join();
    }

    @Test
    public void sendTenMessagesAsynchronouslyWith1PooledThreadAsASmallPerformanceTest() {
        final ForkJoinPool pool = new ForkJoinPool(1);
        final SmtpAuthEmailSender mySender = createSender(pool);

        final List<String> expectedSubjects = IntStream.range(1, 11)
                .mapToObj(i -> "Message "+i)
                .collect(Collectors.toList());
        TestUtils.sendMessages(greenMail, mySender,
                sender -> {
                    TestUtils.sendMessages(sender, 0, 10).toCompletableFuture().join();
                },
                resultAssert -> {
                    resultAssert.containsExactly(expectedSubjects.toArray());
                }
        );
    }

    private static class SlowButFinishingMessageEditor implements MessageEditor {

        private boolean finished = false;

        @Override
        public synchronized void edit(@Nonnull final MimeMessage message) throws Exception {
            TestUtils.validShortEmail().edit(message);
            wait(200);
            finished = true;
        }
    }

}
