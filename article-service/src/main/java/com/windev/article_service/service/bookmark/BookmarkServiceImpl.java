package com.windev.article_service.service.bookmark;

import com.windev.article_service.client.UserClient;
import com.windev.article_service.dto.response.BookmarkDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import com.windev.article_service.dto.response.UserDto;
import com.windev.article_service.entity.Article;
import com.windev.article_service.entity.Bookmark;
import com.windev.article_service.exception.GlobalException;
import com.windev.article_service.mapper.BookmarkMapper;
import com.windev.article_service.repository.ArticleRepository;
import com.windev.article_service.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    private final ArticleRepository articleRepository;

    private final BookmarkMapper bookmarkMapper;

    private final UserClient userClient;

    @Override
    public BookmarkDto createBookmark(Long articleId) {
        Article existingArticle = articleRepository
                .findById(articleId)
                .orElseThrow(() -> new GlobalException("Article with id: " + articleId + " not found.", HttpStatus.NOT_FOUND));

        UserDto user = userClient.getCurrentUser().getBody();

        Bookmark bookmark = Bookmark.builder()
                .article(existingArticle)
                .userId(user.getId())
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);

        log.info("createBookmark() --> bookmark created successfully: {}", bookmark.toString());
        return bookmarkMapper.toDto(savedBookmark);
    }

    @Override
    public PaginatedResponseDto<BookmarkDto> getAllBookmarksByUserId(Long userId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId, pageable);

        List<BookmarkDto> bookmarkDtos = bookmarks.getContent().stream().map(bookmarkMapper::toDto).toList();

        return new PaginatedResponseDto<>(
                bookmarkDtos,
                bookmarks.getNumber(),
                bookmarks.getSize(),
                bookmarks.getTotalPages(),
                bookmarks.getTotalElements(),
                bookmarks.isLast());
    }

    @Override
    public void deleteBookmark(Long bookmarkId) {
        Bookmark existingBookmark = bookmarkRepository
                .findById(bookmarkId)
                .orElseThrow(() -> new GlobalException("Bookmark with id: " + bookmarkId + " not found.", HttpStatus.NOT_FOUND));

        bookmarkRepository.delete(existingBookmark);
        log.info( "Bookmark with id: {} deleted successfully.", bookmarkId);
    }
}
