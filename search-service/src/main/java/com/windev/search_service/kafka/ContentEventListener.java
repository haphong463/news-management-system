package com.windev.search_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windev.search_service.event.Event;
import com.windev.search_service.model.ArticleDocument;
import com.windev.search_service.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContentEventListener {
    private final ArticleService articleService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ContentEvents", groupId = "search-service-group")
    public void listenContentEvents(String message){
        try {
            Event event = objectMapper.readValue(message, Event.class);
            log.info("--> received event: {}", event);

            switch(event.getAction()){
                case "CREATED":
                case "UPDATED":
                    ArticleDocument articleDocument = articleService.fetchArticleById(event.getContentId());
                    if(articleDocument != null){
                        articleService.saveArticle(articleDocument);
                        log.info("--> Article {} has been saved/updated in Elasticsearch.", articleDocument.toString());
                    }
                    break;
                case "DELETED":
                    articleService.deleteArticleById(event.getContentId());
                    log.info("--> Article {} has been deleted from Elasticsearch.", event.getContentId());
                    break;
                default:
                    log.warn("--> Unknown action: {}", event.getAction());
            }


        }catch(Exception e){
            log.error("--> Failed to process content event", e);
        }
    }
}
