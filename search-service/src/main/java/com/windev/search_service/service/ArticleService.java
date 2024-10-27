package com.windev.search_service.service;

import com.windev.search_service.model.ArticleDocument;
import com.windev.search_service.repository.ArticleDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleDocumentRepository articleDocumentRepository;
    private final RestTemplate restTemplate;


    // save article
    public ArticleDocument saveArticle(ArticleDocument articleDocument){
        return articleDocumentRepository.save(articleDocument);
    }

    // delete article
    public void deleteArticleById(Long id){
         articleDocumentRepository.deleteById(id);
    }

    // search by title
    public List<ArticleDocument> searchByTitle(String title){
        return articleDocumentRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<ArticleDocument> searchByAuthorId(Long authorId){
        return articleDocumentRepository.findByAuthorId(authorId);
    }

    public List<ArticleDocument> searchByCategoriesIn(Set<String> categories){
        return articleDocumentRepository.findByCategoriesIn(categories);
    }

    public  List<ArticleDocument> searchByTitleAndAuthor(String title, Long authorId){
        return articleDocumentRepository.findByTitleContainingIgnoreCaseAndAuthorId(title, authorId);
    }

    public List<ArticleDocument> searchArticles(String title, Long authorId, Set<String> categories){
        if(title != null && authorId != null && categories != null){
            return articleDocumentRepository.findByTitleContainingIgnoreCaseAndAuthorIdAndCategoriesIn(title, authorId, categories);
        }else if(title != null && authorId != null){
            return articleDocumentRepository.findByTitleContainingIgnoreCaseAndAuthorId(title, authorId);
        }else if(title != null){
            return articleDocumentRepository.findByTitleContainingIgnoreCase(title);
        }else if(authorId != null){
            return articleDocumentRepository.findByAuthorId(authorId);
        }else if(categories != null){
            return articleDocumentRepository.findByCategoriesIn(categories);
        }else{
            return (List<ArticleDocument>) articleDocumentRepository.findAll();
        }
    }

    public ArticleDocument fetchArticleById(Long id){
        try {
            String url = "http://localhost:8082/api/v1/articles/" + id;
            ResponseEntity<ArticleDocument> response = restTemplate.getForEntity(url, ArticleDocument.class);
            if(response.getStatusCode().is2xxSuccessful()){
                return response.getBody();
            }else{
                log.warn("--> Failed to fetch article with id: {}", id);
                return null;
            }
        }catch(Exception e){
            log.error("--> Exception while fetching article with id: {}", id);
            return null;
        }
    }

}
