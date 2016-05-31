import io.commercetools.sunrise.email.smtp.SmtpAuthEmailSender;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void send2x20MessagesConcurrentlyFromTwoThreadsVia1PooledThreadToAssertThreadSafety() {
        final ForkJoinPool pool = new ForkJoinPool(1);
        final SmtpAuthEmailSender mySender = createSender(pool);

        final List<String> expectedSubjects = IntStream.range(1, 41)
                .mapToObj(i -> "Message "+i)
                .collect(Collectors.toList());

        TestUtils.sendMessages(greenMail, mySender,
                sender -> {
                    final Thread thread = new Thread(() -> {
                        TestUtils.sendMessages(sender, 20, 20).toCompletableFuture().join();
                    });
                    thread.start();
                    TestUtils.sendMessages(sender, 0, 20).toCompletableFuture().join();
                    join(thread);
                },
                resultAssert -> {
                    resultAssert.containsExactlyInAnyOrder(expectedSubjects.toArray());
                }
        );

    }

    private void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}
