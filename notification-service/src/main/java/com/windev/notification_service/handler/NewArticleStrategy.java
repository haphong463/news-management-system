package com.windev.notification_service.handler;

import com.windev.notification_service.client.ArticleClient;
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

    @Autowired
    private ArticleClient articleClient;

    @Override
    public void sendNotification(Object data) {
        if (!(data instanceof NewArticleEvent event)) {
            log.error("Invalid data type for NewArticleStrategy");
            return;
        }

        log.info("--> Send a new article announcement email: {}", data);

        String to = event.getEmail();
        String subject = "New article from " + event.getFirstName() + " " + event.getLastName();

        // Define placeholders and their corresponding values
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("Recipient Name", event.getEmail());
        placeholders.put("Article Title", event.getTitle());
        placeholders.put("Short Description of the Article...", "test short description");
        placeholders.put("Article Link", "http://localhost:3000/articles/" + event.getSlug());
        placeholders.put("Company Name", "The Medium News");
        placeholders.put("Company Address", "123 ABC Street, City XYZ");
        placeholders.put("Contact Information", "contact@yourcompany.com");

        try {
            String htmlContent = emailService.getTemplateContent("templates/new_article.html", placeholders);
            emailService.sendEmailHtml(to, subject, htmlContent);
            log.info("New Article email sent to {}", to);
        } catch (IOException e) {
            log.error("Error reading email template", e);
        } catch (MessagingException e) {
            log.error("Error sending email", e);
        }
    }
}
