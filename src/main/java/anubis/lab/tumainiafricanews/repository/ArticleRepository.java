package anubis.lab.tumainiafricanews.repository;

import anubis.lab.tumainiafricanews.entity.Article;
import anubis.lab.tumainiafricanews.enums.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    // Recherche par slug (unique)
    Optional<Article> findBySlug(String slug);

    // Recherche par titre ou contenu (full-text like)
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.summary) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> searchPublishedArticles(@Param("search") String search, Pageable pageable);

    // Articles d'un auteur
    Page<Article> findByAuthor_UsernameAndStatusOrderByCreatedAtDesc(
            String username, ArticleStatus status, Pageable pageable);

    // Articles en featured / breaking
    List<Article> findTop5ByStatusAndFeaturedTrueOrderByPublishedAtDesc(ArticleStatus status);
    List<Article> findTop3ByStatusAndBreakingTrueOrderByPublishedAtDesc(ArticleStatus status);

    // Compter les articles par statut
    long countByStatus(ArticleStatus status);

    // Vérifier si le slug existe déjà (sauf pour l'article courant)
    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Article> findAll(Specification<Article> spec, Pageable pageable);

    //FOR HOMEPAGE
    // Articles publiés (pour le front public)
    // Page<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    // Articles publiés par catégorie
    // Page<Article> findByStatusAndCategory_SlugOrderByPublishedAtDesc(ArticleStatus status, String categorySlug, Pageable pageable);

    Optional<Article> findTopByStatusAndFeaturedTrueOrderByPublishedAtDesc(ArticleStatus status);

    List<Article> findTopByStatusAndBreakingTrueOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Page<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Page<Article> findByStatusOrderByViewCountDesc(ArticleStatus status, Pageable pageable);

    // Pour catégorie
    Page<Article> findByStatusAndCategory_SlugOrderByPublishedAtDesc(ArticleStatus status, String slug, Pageable pageable);

    // Pour détail article
    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);
}
