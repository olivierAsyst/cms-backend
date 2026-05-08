package anubis.lab.tumainiafricanews.dto.article.request;

import java.util.Set;

public record ArticleCreateRequest(
        String title,
        String summary,
        String content,
        Long categoryId,
        Set<String> tags,           // noms des tags
        boolean featured,
        boolean breaking
) {}
