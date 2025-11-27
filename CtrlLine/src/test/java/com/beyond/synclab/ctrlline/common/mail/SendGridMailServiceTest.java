package com.beyond.synclab.ctrlline.common.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.common.property.SendGridProperties;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class SendGridMailServiceTest {

    private SendGrid sendGrid;
    private SendGridMailService mailService;
    private SendGridProperties sendGridProperties;

    @BeforeEach
    void setUp() {
        this.sendGrid = Mockito.mock(SendGrid.class);
        this.sendGridProperties = new SendGridProperties(
                "dummy",
                new SendGridProperties.Sender("verified@example.com", "CtrlLine")
        );
        this.mailService = new SendGridMailService(sendGrid, sendGridProperties);
    }

    @Test
    void send_shouldInvokeSendGridApiWithExpectedPayload() throws Exception {
        Response response = new Response();
        response.setStatusCode(202);
        when(sendGrid.api(any())).thenReturn(response);

        MailSendRequest request = MailSendRequest.text("user@example.com", "테스트 제목", "본문 내용");

        mailService.send(request);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(captor.capture());

        Request actualRequest = captor.getValue();
        assertThat(actualRequest.getEndpoint()).isEqualTo("mail/send");
        assertThat(actualRequest.getBody())
                .contains("\"email\":\"user@example.com\"")
                .contains("\"subject\":\"테스트 제목\"")
                .contains("\"email\":\"verified@example.com\"");
    }

    @Test
    void send_whenSendGridApiFails_shouldWrapException() throws Exception {
        when(sendGrid.api(any())).thenThrow(new IOException("SendGrid down"));

        MailSendRequest request = MailSendRequest.text("user@example.com", "subject", "body");

        assertThatThrownBy(() -> mailService.send(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SendGrid mail sending failed");
    }
}
