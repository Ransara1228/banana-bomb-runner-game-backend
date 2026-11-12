package com.bbr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * Checks the mail settings (from .env or environment variables) when the app starts,
 * so a wrong login is easy to spot in the log.
 * Gmail shows the App Password as "abcd efgh ijkl mnop", so the spaces are removed here.
 */
@Component
public class MailSetup {

    private static final Logger log = LoggerFactory.getLogger(MailSetup.class);
    private final JavaMailSender sender;

    public MailSetup(JavaMailSender sender) {
        this.sender = sender;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void check() {
        if (!(sender instanceof JavaMailSenderImpl mail)) return;
        String user = mail.getUsername() == null ? "" : mail.getUsername().trim();
        String pass = mail.getPassword() == null ? "" : mail.getPassword().replace(" ", "");
        boolean gmail = mail.getHost() != null && mail.getHost().contains("gmail");
        mail.setUsername(user);
        mail.setPassword(pass);
        if (user.isEmpty()) {
            log.warn("No mail login is set (MAIL_USER in .env or an environment variable), so OTP emails are not sent "
                    + "(the code is only in this log).");
        } else if (pass.isEmpty() || pass.startsWith("PUT") || pass.startsWith("your-")) {
            log.warn("The mail password (MAIL_PASS) is not filled in yet, so OTP emails are not sent.");
        } else if (gmail && pass.length() != 16) {
            log.warn("The Gmail password has {} characters. Put the 16-letter App Password from "
                    + "https://myaccount.google.com/apppasswords in MAIL_PASS (not your normal Gmail "
                    + "password, and not a Google Authenticator code).", pass.length());
        } else {
            log.info("Email is set up: OTP codes are sent through {} as {}", mail.getHost(), user);
        }
    }
}
