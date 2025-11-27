package com.beyond.synclab.ctrlline.common.mail;

import com.beyond.synclab.ctrlline.common.property.SendGridProperties;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendGridMailService implements MailService {

    private final SendGrid sendGrid;
    private final SendGridProperties sendGridProperties;

    @Override
    public void send(MailSendRequest request) {
        Mail mail = buildMail(request);
        Request sgRequest = new Request();
        sgRequest.setMethod(Method.POST);
        sgRequest.setEndpoint("mail/send");
        try {
            sgRequest.setBody(mail.build());
            Response response = sendGrid.api(sgRequest);
            log.debug("SendGrid response status={}, body={}, headers={}",
                    response.getStatusCode(), response.getBody(), response.getHeaders());
        } catch (Exception e) {
            throw new IllegalStateException("SendGrid mail sending failed", e);
        }
    }

    private Mail buildMail(MailSendRequest request) {
        Email from = new Email(
                sendGridProperties.getSender().email(),
                sendGridProperties.getSender().name()
        );
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(request.subject());

        Email to = new Email(request.to());
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        mail.addPersonalization(personalization);

        String contentType = request.html() ? "text/html" : "text/plain";
        mail.addContent(new Content(contentType, request.body()));

        return mail;
    }
}
