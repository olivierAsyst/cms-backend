package anubis.lab.tumainiafricanews.dto.article.response;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleListHomeResponse(
    Long id,
    String title,
    String slug,
    String summary,
    String mainImageUrl,
    String authorUsername,
    String categoryName,
    List<String> tags,
    LocalDateTime publishedAt,
    int viewCount,
    boolean featured,
    boolean breaking
){
}
