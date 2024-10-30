package com.windev.notification_service.handler;

import com.windev.notification_service.event.NewArticleEvent;
import com.windev.notification_service.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("new-article-strategy")
@Slf4j
public class NewArticleStrategy implements NotificationStrategy {

    @Autowired
    private EmailService emailService;

    @Override
    public void sendNotification(Object data) {
        if (!(data instanceof NewArticleEvent)) {
            log.error("Invalid data type for NewArticleStrategy");
            return;
        }

        NewArticleEvent event = (NewArticleEvent) data;
        log.info("--> Send a new article announcement email: {}", data);

        String to = "test123@gmail.com";
        String subject = "New Article from [[Company Name]]";

        // Define placeholders and their corresponding values
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("Recipient Name", "test mail");
        placeholders.put("Article Title", "test article");
        placeholders.put("Short Description of the Article...", "test short description");
        placeholders.put("Article Link", "test link");
        placeholders.put("Company Name", "Your Company Name");
        placeholders.put("Company Address", "123 ABC Street, City XYZ");
        placeholders.put("Contact Information", "contact@yourcompany.com");

        try {
            String htmlContent = emailService.getTemplateContent("email-templates/new_article.html", placeholders);
            emailService.sendEmailHtml(to, subject, htmlContent);
            log.info("New Article email sent to {}", to);
        } catch (IOException e) {
            log.error("Error reading email template", e);
        } catch (MessagingException e) {
            log.error("Error sending email", e);
        }
    }
}
