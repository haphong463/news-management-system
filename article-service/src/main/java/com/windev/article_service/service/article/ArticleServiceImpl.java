package com.windev.article_service.service.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import com.windev.article_service.client.UserClient;
import com.windev.article_service.dto.request.article.CreateArticleRequest;
import com.windev.article_service.dto.request.article.UpdateArticleRequest;
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
import com.windev.article_service.specification.ArticleSpecification;
import com.windev.article_service.util.FileUpload;
import com.windev.article_service.util.SimpleRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.hibernate.sql.Update;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_CACHE_PREFIX = "article:";
    private static final String CATEGORY_NAME_DEFAULT = "Tin tức";
    private static final String RSS_FEED_URL = "https://vnexpress.net/rss/tin-moi-nhat.rss";
    private static final String NOTIFICATION_TOPIC = "notifications";



    @Override
    public ArticleDto getArticleById(Long articleId) {
        String cacheKey = ARTICLE_CACHE_PREFIX + articleId;

        ArticleDto cachedArticle = (ArticleDto) redisTemplate.opsForValue().get(cacheKey);
        if (cachedArticle != null) {
            log.info("getArticleById() --> Returning cached article for ID: {}", articleId);
            return cachedArticle;
        }

        Article article = articleRepository.findById(articleId).orElseThrow((() -> new GlobalException("--> Not found article with id: " + articleId, HttpStatus.NOT_FOUND)));

        ArticleDto articleDto = articleMapper.toDto(article);

        redisTemplate.opsForValue().set(cacheKey, articleDto);
        log.info("getArticleById() --> Article cached with id: {}", articleId);

        return articleDto;
    }

    @Override
    @Transactional
    public ArticleDto createArticle(CreateArticleRequest createArticleRequest) {
        Article article = prepareNewArticle(createArticleRequest);

        Article savedArticle = articleRepository.save(article);
        log.info("createArticle() --> CREATE ARTICLE OK: {}", savedArticle.getTitle());

        ArticleDto articleDto = articleMapper.toDto(savedArticle);
        String cacheKey = ARTICLE_CACHE_PREFIX + savedArticle.getId();

        redisTemplate.opsForValue().set(cacheKey, articleDto);

        invalidateSearchCaches();

        // send event to notifications topic
        sendContentEvent(savedArticle, "CREATED");

        return articleDto;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<ArticleDto> getAllArticles(Pageable pageable) {
        return convertToPaginatedResponseDto(articleRepository.findAll(pageable));
    }

    @Override
    public PaginatedResponseDto<ArticleDto> searchArticles(String title, Long authorId, Set<String> categories, Pageable pageable) {
        Specification<Article> specification = Specification.where(null);
        if (title != null && !title.isEmpty()) {
            specification = specification.and(ArticleSpecification.hasTitleContaining(title));
        }

        if (authorId != null) {
            specification = specification.and(ArticleSpecification.hasAuthorId(authorId));
        }

        if (categories != null && !categories.isEmpty()) {
            specification = specification.and(ArticleSpecification.hasCategoriesIn(categories));
        }

        Page<Article> articles = articleRepository.findAll(specification, pageable);

        return convertToPaginatedResponseDto(articles);
    }

    @Override
    @Transactional
    public ArticleDto updateArticle(Long articleId, UpdateArticleRequest updateArticleRequest) {
        Article existingArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new GlobalException("Article with ID: " + articleId + " not found.", HttpStatus.NOT_FOUND));

        updateTitleAndContent(existingArticle, updateArticleRequest);

        if (updateArticleRequest.getMainImage() != null && !updateArticleRequest.getMainImage().isEmpty()) {
            updateImages(existingArticle, updateArticleRequest.getMainImage());
        }

        if (updateArticleRequest.getCategoryNames() != null && !updateArticleRequest.getCategoryNames().isEmpty()) {
            List<Category> validatedCategories = validateCategories(updateArticleRequest.getCategoryNames());
            existingArticle.setCategories(validatedCategories);
        }

        Article savedArticle = articleRepository.save(existingArticle);
        log.info("updateArticle() --> Article updated successfully: {}", savedArticle.getTitle());

        updateArticleCache(savedArticle);

        return articleMapper.toDto(savedArticle);
    }


    @Override
    @Transactional
    public String deleteArticle(Long articleId) {
        Article existingArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new GlobalException("Not found article with id: " + articleId, HttpStatus.NOT_FOUND));

        articleRepository.delete(existingArticle);

        String cacheKey = ARTICLE_CACHE_PREFIX + articleId;

        redisTemplate.delete(cacheKey);
        log.info("deleteArticle() --> Delete article with ID: {}", articleId);

        return "Article deleted successfully!";
    }

    @Override
    @Transactional
    public String crawlNewsData() {

        try {
            Document doc = Jsoup.connect(RSS_FEED_URL).get();
            Elements items = doc.select("item");

            Category category = categoryRepository.findByName(CATEGORY_NAME_DEFAULT)
                    .orElseThrow(() -> new GlobalException("Category not found with name: " + CATEGORY_NAME_DEFAULT, HttpStatus.NOT_FOUND));

            List<Category> categories = new ArrayList<>();
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


    // ===================== Helper Methods =====================

    private void updateTitleAndContent(Article article, UpdateArticleRequest request) {
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            article.setTitle(request.getTitle());
            article.setSlug(Slugify.builder().build().slugify(request.getTitle()));
        }
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            article.setContent(request.getContent());
        }
    }

    private void updateImages(Article article, MultipartFile mainImageFile) {
        // Lưu hình ảnh chính
        String mainImagePath = FileUpload.saveImage(mainImageFile);
        article.setMainImage(mainImagePath);

        // Tạo và lưu hình thu nhỏ
        String thumbnailPath = FileUpload.createThumbnail(mainImagePath);
        article.setThumbnailImage(thumbnailPath);

        log.info("updateImages() --> Images updated: mainImage={}, thumbnailImage={}", mainImagePath, thumbnailPath);
    }

    private void updateArticleCache(Article article) {
        String cacheKey = ARTICLE_CACHE_PREFIX + article.getId();
        ArticleDto articleDto = articleMapper.toDto(article);
        redisTemplate.opsForValue().set(cacheKey, articleDto);
        log.info("updateArticleCache() --> Cache updated for article ID: {}", article.getId());

        invalidateSearchCaches();
    }

    private void sendContentEvent(Article article, String action) {
        try {
            ContentEvent event = new ContentEvent();
            event.setContentId(article.getId());
            event.setTitle(article.getTitle());
            event.setAction(action);
            event.setAuthorId(article.getAuthorId());

            String eventAsString = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(NOTIFICATION_TOPIC, eventAsString);
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
    private Article prepareNewArticle(CreateArticleRequest createArticleRequest) {
        final Slugify slugify = Slugify.builder().build();
        ResponseEntity<UserDto> response = userClient.getCurrentUser();
         String thumbnailPath = "";
            String mainImagePath = "";

            if (createArticleRequest.getMainImage() != null && !createArticleRequest.getMainImage().isEmpty()) {
                mainImagePath = FileUpload.saveImage(createArticleRequest.getMainImage());

                // Tạo thumbnail từ ảnh chính
                thumbnailPath = FileUpload.createThumbnail(mainImagePath);
            }


            Article article = Article.builder()
                    .title(createArticleRequest.getTitle())
                    .content(createArticleRequest.getContent())
                    .slug(slugify.slugify(createArticleRequest.getTitle()))
                    .authorId(response.getBody().getId())
                    .thumbnailImage(thumbnailPath)
                    .mainImage(mainImagePath)
                    .build();

            List<Category> validatedCategories = validateCategories(createArticleRequest.getCategoryNames());
            article.setCategories(validatedCategories);
            return article;

    }

    private List<Category> validateCategories(Set<String> categoryNames){
        List<Category> validatedCategories = new ArrayList<>();
        for (String category : categoryNames) {
            Category existingCategory = categoryRepository.findByName(category)
                    .orElseThrow(() -> {
                        log.warn("createArticle() --> CATEGORY NOT FOUND!");
                        return new GlobalException("Category not found: " + category, HttpStatus.NOT_FOUND);
                    });
            validatedCategories.add(existingCategory);
        }
        return validatedCategories;
    }

    private void invalidateSearchCaches() {
        Set<String> keys = redisTemplate.keys("search:title:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("invalidateSearchCaches() --> Cleared search caches");
        }
    }
}
