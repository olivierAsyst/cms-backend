package anubis.lab.tumainiafricanews.specification;

import anubis.lab.tumainiafricanews.entity.Article;
import anubis.lab.tumainiafricanews.enums.ArticleStatus;
import org.springframework.data.jpa.domain.Specification;

public class ArticleSpecification {

    public static Specification<Article> withStatus(ArticleStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Article> withFeatured(Boolean featured) {
        return (root, query, cb) ->
                featured == null ? null : cb.equal(root.get("featured"), featured);
    }

    public static Specification<Article> withCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Article> withAuthor(String username) {
        return (root, query, cb) ->
                username == null ? null : cb.equal(root.get("author").get("username"), username);
    }


    public static Specification<Article> isFeatured() {
        return (root, query, cb) -> cb.equal(root.get("featured"), true);
    }

    public static Specification<Article> search(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) return null;

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("summary")), pattern)
            );
        };
    }


}