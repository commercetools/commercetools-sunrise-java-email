package com.commercetools.sunrise.email.smtp;

import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class AttachmentIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void sendAttachmentWorksEvenWithUmlauts() throws Exception {
        final String subject = "Test with attachment";
        final String recipient = "foo@domain.de";
        final BodyPart textPart = new MimeBodyPart();
        textPart.setText("Hello ättächment!");
        final BodyPart attachmentPart = new MimeBodyPart();
        final String attachmentFileName = "sampleAttachment.txt";
        final URL attachmentURL = getClass().getClassLoader().getResource(attachmentFileName);
        assertThat(attachmentURL).isNotNull();
        attachmentPart.setDataHandler(new DataHandler(new URLDataSource(attachmentURL)));
        attachmentPart.setFileName(attachmentFileName);
        final MimeMultipart sentContent = new MimeMultipart(textPart, attachmentPart);

        final String messageId = sender.send(msg -> {
            msg.addRecipients(Message.RecipientType.TO, recipient);
            msg.setSubject(subject, "UTF-8");
            msg.setContent(sentContent);
        }).toCompletableFuture().join();
        final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        assertThat(messageId).isNotEmpty();
        assertThat(receivedMessages).hasSize(1);

        final MimeMessage message = receivedMessages[0];
        assertThat(message.getSubject()).isEqualTo(subject);
        assertThat(message.getAllRecipients()).hasSize(1);
        assertThat(message.getAllRecipients()[0]).isEqualTo(new InternetAddress(recipient));
        assertThat(message.getContent()).isInstanceOf(MimeMultipart.class);

        final MimeMultipart receivedContent = (MimeMultipart)message.getContent();
        assertThat(receivedContent.getCount()).isEqualTo(2);

        final BodyPart part0 = receivedContent.getBodyPart(0);
        assertThat(part0.getContentType()).isEqualTo("text/plain; charset=UTF-8");
        assertThat(part0.getContent()).isEqualTo("Hello ättächment!");

        final BodyPart part1 = receivedContent.getBodyPart(1);
        assertThat(part1.getContentType()).isEqualTo("text/plain; charset=UTF-8; name=sampleAttachment.txt");
        assertThat(part1.getDisposition()).isEqualTo("attachment");
        assertThat(part1.getContent()).isEqualTo("A sample attachment for testing purposes with äöüß.");
    }

}
