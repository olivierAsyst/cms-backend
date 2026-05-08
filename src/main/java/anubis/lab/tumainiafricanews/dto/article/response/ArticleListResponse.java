package anubis.lab.tumainiafricanews.dto.article.response;

import java.time.LocalDateTime;

public record ArticleListResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String mainImageUrl,
        String authorUsername,
        String categoryName,
        LocalDateTime publishedAt,
        int viewCount
) {}
