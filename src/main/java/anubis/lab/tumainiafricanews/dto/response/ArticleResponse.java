package anubis.lab.tumainiafricanews.dto.response;

import anubis.lab.tumainiafricanews.enums.ArticleStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String content,
        String mainImageUrl,
        List<String> imageUrls,
        String authorUsername,
        String categoryName,
        List<String> tags,
        ArticleStatus status,
        LocalDateTime publishedAt,
        int viewCount,
        boolean featured,
        boolean breaking
) {}
