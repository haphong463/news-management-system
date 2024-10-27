package com.windev.search_service.controller;

import com.windev.search_service.model.ArticleDocument;
import com.windev.search_service.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<?> searchArticles(@RequestParam("title") String title
){

        try {
            List<ArticleDocument> articleDocuments = articleService.searchArticles(title, null, null);
            return new ResponseEntity<>(articleDocuments, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
