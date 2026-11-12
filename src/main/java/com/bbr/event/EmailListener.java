package com.bbr.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailListener {

    private static final Logger log = LoggerFactory.getLogger(EmailListener.class);
    private final JavaMailSender mail;
    private final String from;      // the address the code comes from (app.mail.from, else the mail login)
    private final boolean logCode;  // app.otp.log-code: show the code in the console while testing

    public EmailListener(JavaMailSender mail,
                         @Value("${app.mail.from:${spring.mail.username:}}") String from,
                         @Value("${app.otp.log-code:false}") boolean logCode) {
        this.mail = mail;
        this.from = from == null ? "" : from.trim();
        this.logCode = logCode;
    }

    @Async          // runs in the background, so login does not wait for the email
    @EventListener
    public void onOtpRequested(OtpRequestedEvent e) {
        if (logCode) log.info("DEV ONLY - OTP for {} is {}", e.email(), e.code());   // off in the final version
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (!from.isEmpty()) msg.setFrom("Banana Bomb Runner <" + from + ">");
            msg.setTo(e.email());
            msg.setSubject("Your Banana Bomb Runner code");
            msg.setText("Hi!\n\nYour Banana Bomb Runner login code is " + e.code() + ".\n"
                    + "It works for 5 minutes. If you did not try to log in, you can ignore this email.");
            mail.send(msg);
            log.info("OTP email sent to {}", e.email());
        } catch (MailAuthenticationException ex) {
            log.warn("Email not sent: the mail server did not accept MAIL_USER / MAIL_PASS. "
                    + "Gmail needs a 16-letter App Password; Brevo needs its SMTP key.");
        } catch (Exception ex) {
            log.warn("Email not sent: {}", ex.getMessage());
        }
    }
}
