package anubis.lab.tumainiafricanews.dto.article.request;

import java.util.Set;

public record ArticleUpdateRequest(
        String title,
        String summary,
        String content,
        Long categoryId,
        Set<String> tags,
        boolean featured,
        boolean breaking
) {}

