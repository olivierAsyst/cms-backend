package anubis.lab.tumainiafricanews.dto.response;

import anubis.lab.tumainiafricanews.enums.ArticleStatus;

import java.time.LocalDateTime;

public record ArticleAdminListResponse(
        Long id,
        String title,
        String slug,
        ArticleStatus status,
        String authorUsername,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        int viewCount,
        boolean featured
) {}
