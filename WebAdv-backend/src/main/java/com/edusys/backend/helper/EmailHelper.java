package com.edusys.backend.helper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailHelper {
    private final static Logger logger = LoggerFactory.getLogger(EmailHelper.class);

    @Value("${app.integrations.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.integrations.mail.from:}")
    private String fromAddress;

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String text, String subject, boolean html, String... to_email) throws MessagingException {
        if (!mailEnabled) {
            throw new IllegalStateException("Mail integration is disabled");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Mail sender address is not configured");
        }

        messageHelper.setFrom(fromAddress);
        messageHelper.setTo(to_email);
        messageHelper.setSubject(subject);
        messageHelper.setText(text, html);

        mailSender.send(message);
        logger.debug("Email sent to {} recipient(s)", to_email.length);
    }
}
