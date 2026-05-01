package anubis.lab.tumainiafricanews.mappers;

import anubis.lab.tumainiafricanews.dto.request.ArticleCreateRequest;
import anubis.lab.tumainiafricanews.dto.request.ArticleUpdateRequest;
import anubis.lab.tumainiafricanews.dto.response.ArticleAdminListResponse;
import anubis.lab.tumainiafricanews.dto.response.ArticleListResponse;
import anubis.lab.tumainiafricanews.dto.response.ArticleResponse;
import anubis.lab.tumainiafricanews.entity.Article;
import anubis.lab.tumainiafricanews.entity.ArticleImage;
import anubis.lab.tumainiafricanews.entity.Category;
import anubis.lab.tumainiafricanews.entity.Tag;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.enums.ArticleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ArticleMapper {

    public Article toEntity(ArticleCreateRequest request, User author, Category category, Set<Tag> tags) {
        return Article.builder()
                .title(request.title())
                .slug(generateSlug(request.title()))
                .summary(request.summary())
                .content(request.content())
                .author(author)
                .category(category)
                .tags(tags != null ? tags : new HashSet<>())
                .featured(request.featured())
                .breaking(request.breaking())
                .status(ArticleStatus.DRAFT)
                .viewCount(0)
                .build();
    }

    public ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContent(),
                article.getMainImageUrl(),
                article.getImages().stream()
                        .sorted(Comparator.comparing(ArticleImage::getPosition))
                        .map(ArticleImage::getUrl)
                        .toList(),
                article.getAuthor().getUsername(),
                article.getCategory() != null ? article.getCategory().getName() : null,
                article.getTags().stream().map(Tag::getName).toList(),
                article.getStatus(),
                article.getPublishedAt(),
                article.getViewCount(),
                article.isFeatured(),
                article.isBreaking()
        );
    }

    public ArticleListResponse toListResponse(Article article) {
        return new ArticleListResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getMainImageUrl(),
                article.getAuthor().getUsername(),
                article.getCategory() != null ? article.getCategory().getName() : null,
                article.getPublishedAt(),
                article.getViewCount()
        );
    }

    public ArticleAdminListResponse toAdminListResponse(Article article) {
        return new ArticleAdminListResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getStatus(),
                article.getAuthor().getUsername(),
                article.getCategory() != null ? article.getCategory().getName() : null,
                article.getCreatedAt(),
                article.getPublishedAt(),
                article.getViewCount(),
                article.isFeatured()
        );
    }

    public void updateArticleFromRequest(Article article, ArticleUpdateRequest request, Category category, Set<Tag> tags) {
        article.setTitle(request.title());
        article.setSummary(request.summary());
        article.setContent(request.content());
        article.setCategory(category);
        article.setTags(tags != null ? tags : new HashSet<>());
        article.setFeatured(request.featured());
        article.setBreaking(request.breaking());
    }

    public String generateSlug(String title) {
        if (title == null) return "";

        return title.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}