package com.windev.article_service.controller;

import com.windev.article_service.dto.request.bookmark.CreateBookmarkRequest;
import com.windev.article_service.dto.response.BookmarkDto;
import com.windev.article_service.service.bookmark.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/bookmark")
@RequiredArgsConstructor
@RestController
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<?> createBookmark(@RequestBody CreateBookmarkRequest request){
        try {
            BookmarkDto result = bookmarkService.createBookmark(request.getArticleId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{bookmarkId}")
    public ResponseEntity<?> removeBookmark(@PathVariable Long bookmarkId){
        try {
            bookmarkService.deleteBookmark(bookmarkId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
