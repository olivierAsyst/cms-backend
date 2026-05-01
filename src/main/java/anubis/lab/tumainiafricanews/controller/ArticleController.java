package anubis.lab.tumainiafricanews.controller;

import anubis.lab.tumainiafricanews.dto.request.ArticleCreateRequest;
import anubis.lab.tumainiafricanews.dto.request.ArticleUpdateRequest;
import anubis.lab.tumainiafricanews.dto.request.PublishRequest;
import anubis.lab.tumainiafricanews.dto.response.ApiResponse;
import anubis.lab.tumainiafricanews.dto.response.ArticleAdminListResponse;
import anubis.lab.tumainiafricanews.dto.response.ArticleListResponse;
import anubis.lab.tumainiafricanews.dto.response.ArticleResponse;
import anubis.lab.tumainiafricanews.enums.ArticleStatus;
import anubis.lab.tumainiafricanews.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // ====================== PUBLIC ENDPOINTS ======================
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<ArticleListResponse>>> getPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ResponseEntity.ok(articleService.getPublishedArticles(pageable, category, search));
    }

    @GetMapping("/public/{slug}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    // ====================== ADMIN / EDITOR ENDPOINTS ======================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<ArticleResponse>> create(
            @ModelAttribute ArticleCreateRequest request,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal(expression = "username") String username) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(articleService.createArticle(request, username, mainImage, images));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<ArticleResponse>> update(
            @PathVariable Long id,
            @ModelAttribute ArticleUpdateRequest request,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @AuthenticationPrincipal(expression = "username") String username) {

        return ResponseEntity.ok(articleService.updateArticle(id, request, username, mainImage));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<ArticleResponse>> publish(
            @PathVariable Long id,
            @RequestBody(required = false) PublishRequest request) {

        return ResponseEntity.ok(articleService.publishArticle(id, request != null ? request : new PublishRequest(null)));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Page<ArticleAdminListResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(articleService.getAllArticlesForAdmin(pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<ArticleResponse>> getOneForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleForAdmin(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                    @AuthenticationPrincipal(expression = "username") String username) {
        return ResponseEntity.ok(articleService.deleteArticle(id, username));
    }

    // Bonus : Articles par auteur
    @GetMapping("/my-articles")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Page<ArticleAdminListResponse>>> getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ArticleStatus status,
            @AuthenticationPrincipal(expression = "username") String username) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(articleService.getMyArticles(pageable, username, status));
    }
}