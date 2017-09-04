package com.commercetools.sunrise.email.smtp;

import com.commercetools.sunrise.email.EmailDeliveryException;
import org.junit.Test;

import javax.mail.Message;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SendingIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void missingRecipientAddressYieldsEmailDeliveryException() {
        assertThatThrownBy(() -> { sender.send(msg -> {}).toCompletableFuture().join(); })
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(EmailDeliveryException.class)
                .hasStackTraceContaining("No recipient addresses");
    }

    @Test
    public void theExceptionallyMethodOfCompletionStageCanBeUsedToHandleEmailDeliveryException() {
        final String cause = sender.send(msg -> {})
                .exceptionally(throwable -> throwable.getClass().getName())
                .toCompletableFuture().join();
        assertThat(cause).isEqualTo(EmailDeliveryException.class.getName());
    }

    @Test
    public void missingContentYieldsEmailDeliveryException() throws Exception {
        final Method thisMethod = SendingIntegrationTest.class
                .getMethod("missingContentYieldsEmailDeliveryException");
        System.err.println("Note that "+thisMethod.getDeclaringClass().getName()+"."+thisMethod.getName()+"()"
                +" deliberately raises an IllegalStateException / EOFException in the Greenmail implementation"
                +" that you're going to see on System.err in the following.");

        assertThatThrownBy(() -> {
            sender.send(msg -> {
                msg.addRecipients(Message.RecipientType.TO, FOO_BAR_AT_DOMAIN_COM);
            }).toCompletableFuture().join();
        }).hasCauseInstanceOf(EmailDeliveryException.class)
                .hasStackTraceContaining("No MimeMessage content");
    }

    @Test
    public void missingSubjectYieldsEmailDeliveryException() {
        assertThatThrownBy(() -> {
            sender.send(msg -> {
                msg.addRecipients(Message.RecipientType.TO, FOO_BAR_AT_DOMAIN_COM);
                msg.setText(HELLO_WORLD, "UTF-8");
            }).toCompletableFuture().join();
        }).hasCauseInstanceOf(EmailDeliveryException.class)
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
