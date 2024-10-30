package com.windev.article_service.service.bookmark;

import com.windev.article_service.dto.response.BookmarkDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface BookmarkService {
    BookmarkDto createBookmark(Long articleId);
    PaginatedResponseDto<BookmarkDto> getAllBookmarksByUserId(Long userId, Pageable pageable);
    void deleteBookmark(Long bookmarkId);
}
