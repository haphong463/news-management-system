package com.windev.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmailHtml(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true indicates HTML

        mailSender.send(message);
    }

    public String getTemplateContent(String templatePath, Map<String, String> placeholders) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

        // Thay thế các placeholder
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            content = content.replace("[[" + entry.getKey() + "]]", entry.getValue());
        }

        return content;
    }

}
