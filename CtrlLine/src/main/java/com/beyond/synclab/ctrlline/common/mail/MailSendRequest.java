package com.beyond.synclab.ctrlline.common.mail;

public record MailSendRequest(
        String to,
        String subject,
        String body,
        boolean html
) {

    public static MailSendRequest text(String to, String subject, String body) {
        return new MailSendRequest(to, subject, body, false);
    }

    public static MailSendRequest html(String to, String subject, String body) {
        return new MailSendRequest(to, subject, body, true);
    }
}
