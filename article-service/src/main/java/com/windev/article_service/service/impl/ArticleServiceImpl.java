package com.windev.article_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import com.windev.article_service.client.UserClient;
import com.windev.article_service.dto.request.CreateArticleRequest;
import com.windev.article_service.dto.request.UpdateArticleRequest;
import com.windev.article_service.dto.response.ArticleDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import com.windev.article_service.dto.response.UserDto;
import com.windev.article_service.entity.Article;
import com.windev.article_service.entity.Category;
import com.windev.article_service.event.ContentEvent;
import com.windev.article_service.exception.GlobalException;
import com.windev.article_service.mapper.ArticleMapper;
import com.windev.article_service.repository.ArticleRepository;
import com.windev.article_service.repository.CategoryRepository;
import com.windev.article_service.service.ArticleService;
import com.windev.article_service.util.SimpleRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final UserClient userClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SimpleRateLimiter rateLimiter = new SimpleRateLimiter(500);


    @Override
    public ArticleDto createArticle(CreateArticleRequest createArticleRequest) {

        log.info("createArticle() --> CREATE ARTICLE METHOD START");
        Article article = prepareNewArticle(createArticleRequest);

        Article savedArticle = articleRepository.save(article);
        log.info("createArticle() --> CREATE ARTICLE OK: {}", savedArticle.getTitle());

        sendContentEvent(savedArticle,"CREATED");

        return articleMapper.toDto(savedArticle);
    }

    @Override
    public PaginatedResponseDto<ArticleDto> getAllArticles(Pageable pageable) {
        return convertToPaginatedResponseDto(articleRepository.findAll(pageable));
    }

    @Override
    public PaginatedResponseDto<ArticleDto> searchArticlesByTitle(String title, Pageable pageable) {
        return convertToPaginatedResponseDto(articleRepository.findByTitleContainingIgnoreCase(title, pageable));
    }

    @Override
    public PaginatedResponseDto<ArticleDto> getArticlesByAuthor(Long authorId, Pageable pageable) {
        return convertToPaginatedResponseDto(articleRepository.findByAuthorId(authorId, pageable));
    }

    @Override
    public ArticleDto updateArticle(Long articleId, UpdateArticleRequest updateArticleRequest) {
        return null;
    }

    @Override
    public void deleteArticle(Long articleId) {

    }

    @Override
    public String crawlNewsData() {
        String url = "https://vnexpress.net/rss/tin-moi-nhat.rss";

        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.select("item");

            Category category = categoryRepository.findByName("Tin tức").orElseThrow(() -> new GlobalException("Category not found with name: Tin tức", HttpStatus.NOT_FOUND));

            Set<Category> categories = new HashSet<>();
            categories.add(category);

            // Sử dụng ExecutorService với một ThreadPool cố định
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            List<Future<?>> futures = new ArrayList<>();

            for (Element item : items) {
                String urlDetails = item.select("link").first().text();

                Callable<Void> task = () -> {
                    // * apply rate limiter to wait 500ms between calls
                    rateLimiter.acquire();

                    Document docDetails = Jsoup.connect(urlDetails)
                            .userAgent("Mozilla/5.0")
                            .timeout(10 * 1000)
                            .get();

                    String title = docDetails.select("h1.title-detail").text();

                    // ?
                    String description = docDetails.select("p.description").text();


                    Element contentElement = docDetails.selectFirst("article.fck_detail");

                    // ?
                    String contentHtml = contentElement.html();

                    String contentText = contentElement.text();
                    String author = docDetails.select("p.author_mail strong").text();
                    System.out.println("Tác giả: " + author);

                    String publishDate = docDetails.select("span.date").text();
                    System.out.println("Ngày đăng: " + publishDate);

                    Article article = new Article();
                    article.setTitle(title);
                    article.setSlug(Slugify.builder().build().slugify(title));
                    article.setAuthorId(1L);
                    article.setCategories(categories);
                    article.setContent(contentText);

                    articleRepository.save(article);
                    return null;
                };

                futures.add(executorService.submit(task));
            }

            // Chờ tất cả các nhiệm vụ hoàn thành
            for (Future<?> future : futures) {
                future.get();
            }

            executorService.shutdown();

            return "Crawl data successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }



    private void sendContentEvent(Article article, String action) {
        try {
            ContentEvent event = new ContentEvent();
            event.setContentId(article.getId());
            event.setTitle(article.getTitle());
            event.setAction(action);
            event.setAuthorId(article.getAuthorId());
            // Thiết lập các trường khác nếu cần

            String eventAsString = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ContentEvents", eventAsString);
            log.info("sendContentEvent() --> Event sent to Kafka: {}", eventAsString);
        } catch (Exception e) {
            log.error("sendContentEvent() --> Failed to send event to Kafka", e);
            // Xử lý lỗi nếu cần
        }
    }


    private PaginatedResponseDto<ArticleDto> convertToPaginatedResponseDto(Page<Article> articlePage) {
        List<ArticleDto> articles = articlePage.getContent().stream().map(articleMapper::toDto).toList();
        return new PaginatedResponseDto<>(articles, articlePage.getNumber(), articlePage.getSize(),
                articlePage.getTotalPages(), articlePage.getTotalElements(), articlePage.isLast());
    }

    //* PREPARE NEW ARTICLE
    private Article prepareNewArticle(CreateArticleRequest createArticleRequest) {
        final Slugify slugify = Slugify.builder().build();
        ResponseEntity<UserDto> response = userClient.getCurrentUser();
        Article article = new Article();
        article.setTitle(createArticleRequest.getTitle());
        article.setContent(createArticleRequest.getContent());
        article.setSlug(slugify.slugify(createArticleRequest.getTitle()));
        article.setAuthorId(response.getBody().getId());

        Set<Category> validatedCategories = new HashSet<>();
        for (String category : createArticleRequest.getCategoriesId()) {
            Category existingCategory = categoryRepository.findByName(category)
                    .orElseThrow(() -> {
                        log.warn("createArticle() --> CATEGORY NOT FOUND!");
                        return new GlobalException("Category not found: " + category, HttpStatus.NOT_FOUND);
                    });
            validatedCategories.add(existingCategory);
        }
        article.setCategories(validatedCategories);
        return article;
    }
}
