package anubis.lab.tumainiafricanews.controller;

import anubis.lab.tumainiafricanews.dto.article.response.*;
import anubis.lab.tumainiafricanews.dto.response.ApiResponse;
import anubis.lab.tumainiafricanews.service.ArticleService;
import anubis.lab.tumainiafricanews.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final CategoryService categoryService;
    private final ArticleService articleService;

    @GetMapping("/categories/list")
    public ResponseEntity<List<CategoryListResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomePageResponse>> getHomePage() {
        return ResponseEntity.ok(
                ApiResponse.success("Données de la page d'accueil", articleService.getHomePageData())
        );
    }

    @GetMapping("/breaking")
    public ResponseEntity<ApiResponse<List<ArticleListHomeResponse>>> getBreakingNews() {
        return ResponseEntity.ok(
                ApiResponse.success("Actualités urgentes", articleService.getBreakingNews(8))
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ArticleListHomeResponse>>> getPopularArticles() {
        return ResponseEntity.ok(
                ApiResponse.success("Articles les plus lus", articleService.getPopularArticles(10))
        );
    }

    // ==================== ARTICLES PAR CATÉGORIE ====================
    @GetMapping("/category/{slug}")
    public ResponseEntity<ApiResponse<Page<ArticleListHomeResponse>>> getArticlesByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<ArticleListHomeResponse> articles = articleService.getArticlesByCategory(slug, pageable);

        return ResponseEntity.ok(ApiResponse.success("Articles de la catégorie", articles));
    }

    // ==================== ARTICLE DETAIL ====================
    @GetMapping("/articles/{slug}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleBySlug(@PathVariable String slug) {
        ArticleResponse article = articleService.getPublicArticleBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Article récupéré", article));
    }

    // ==================== RECHERCHE ====================
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ArticleListHomeResponse>>> searchArticles(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleListHomeResponse> results = articleService.searchArticles(q, pageable);

        return ResponseEntity.ok(ApiResponse.success("Résultats de recherche", results));
    }

}
