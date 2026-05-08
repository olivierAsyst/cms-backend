package anubis.lab.tumainiafricanews.service;

import anubis.lab.tumainiafricanews.config.MinioService;
import anubis.lab.tumainiafricanews.dto.article.request.ArticleCreateRequest;
import anubis.lab.tumainiafricanews.dto.article.request.ArticleUpdateRequest;
import anubis.lab.tumainiafricanews.dto.article.request.PublishRequest;
import anubis.lab.tumainiafricanews.dto.article.response.*;
import anubis.lab.tumainiafricanews.dto.response.ApiResponse;
import anubis.lab.tumainiafricanews.entity.Article;
import anubis.lab.tumainiafricanews.entity.ArticleImage;
import anubis.lab.tumainiafricanews.entity.Category;
import anubis.lab.tumainiafricanews.entity.Tag;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.enums.ArticleStatus;
import anubis.lab.tumainiafricanews.exception.BusinessException;
import anubis.lab.tumainiafricanews.exception.ResourceNotFoundException;
import anubis.lab.tumainiafricanews.mappers.ArticleMapper;
import anubis.lab.tumainiafricanews.specification.ArticleSpecification;
import anubis.lab.tumainiafricanews.repository.ArticleRepository;
import anubis.lab.tumainiafricanews.repository.CategoryRepository;
import anubis.lab.tumainiafricanews.repository.TagRepository;
import anubis.lab.tumainiafricanews.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = Exception.class)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final ArticleMapper mapper;

    public ApiResponse<ArticleResponse> createArticle(ArticleCreateRequest request, String username, MultipartFile mainImage, List<MultipartFile> additionalImages) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
        }

        Set<Tag> tags = new HashSet<>();
        if (request.tags() != null) {
            for (String tagName : request.tags()) {
                Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(null, tagName, mapper.generateSlug(tagName), new HashSet<>())));
                tags.add(tag);
            }
        }

        Article article = mapper.toEntity(request, author, category, tags);

        // Upload image principale
        if (mainImage != null && !mainImage.isEmpty()) {
            try {
                String imageUrl = minioService.uploadImage(mainImage, "articles/" + LocalDate.now().getYear());
                article.setMainImageUrl(imageUrl);
            } catch (Exception e) {
                throw new BusinessException("Erreur lors de l'upload de l'image principale");
            }
        }

        Article saved = articleRepository.save(article);

        return ApiResponse.success("Article créé avec succès", mapper.toResponse(saved));
    }

    public ApiResponse<ArticleResponse> createArticleII(ArticleCreateRequest request,
                                                      String username,
                                                      MultipartFile mainImage,
                                                      List<MultipartFile> additionalImages) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Category category = request.categoryId() != null ?
                categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée")) : null;

        Set<Tag> tags = resolveTags(request.tags());

        Article article = mapper.toEntity(request, author, category, tags);

        // Upload image principale
        if (mainImage != null && !mainImage.isEmpty()) {
            String imageUrl = uploadImage(mainImage, "main");
            article.setMainImageUrl(imageUrl);
        }

        // Upload images supplémentaires
        if (additionalImages != null) {
            for (int i = 0; i < additionalImages.size(); i++) {
                MultipartFile file = additionalImages.get(i);
                if (!file.isEmpty()) {
                    String url = uploadImage(file, "additional");
                    article.addImage(new ArticleImage(file.getOriginalFilename(), url, "Image " + (i+1), i));
                }
            }
        }

        Article savedArticle = articleRepository.save(article);
        return ApiResponse.success("Article créé avec succès", mapper.toResponse(savedArticle));
    }

    public ApiResponse<ArticleResponse> updateArticle(Long id, ArticleUpdateRequest request,
                                                      String username, MultipartFile mainImage) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé"));

        // Vérification des droits (seul l'auteur ou ADMIN)
        if (!article.getAuthor().getUsername().equals(username) &&
                !SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new BusinessException("Vous n'avez pas les droits pour modifier cet article");
        }

        // Mise à jour des champs
        article.setTitle(request.title());
        article.setSlug(generateUniqueSlug(request.title(), id));
        article.setSummary(request.summary());
        article.setContent(request.content());
        article.setFeatured(request.featured());
        article.setBreaking(request.breaking());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
            article.setCategory(category);
        }

        article.setTags(resolveTags(request.tags()));

        // Mise à jour image principale si fournie
        if (mainImage != null && !mainImage.isEmpty()) {
            if (article.getMainImageUrl() != null) {
                // Supprimer l'ancienne image (optionnel)
            }
            String newImageUrl = uploadImage(mainImage, "main");
            article.setMainImageUrl(newImageUrl);
        }

        Article updated = articleRepository.save(article);
        return ApiResponse.success("Article mis à jour avec succès", mapper.toResponse(updated));
    }

    public ApiResponse<ArticleResponse> publishArticle(Long id, PublishRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé"));

        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishedAt(request.publishedAt() != null ? request.publishedAt() : LocalDateTime.now());

        Article published = articleRepository.save(article);
        return ApiResponse.success("Article publié avec succès", mapper.toResponse(published));
    }

    @Transactional//(readOnly = true)
    public ApiResponse<Page<ArticleListResponse>> getPublishedArticles(Pageable pageable, String categorySlug, String search) {
        Page<Article> articles;

        if (search != null && !search.trim().isEmpty()) {
            articles = articleRepository.searchPublishedArticles(search.trim(), pageable);
        } else if (categorySlug != null) {
            articles = articleRepository.findByStatusAndCategory_SlugOrderByPublishedAtDesc(
                    ArticleStatus.PUBLISHED, categorySlug, pageable);
        } else {
            articles = articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, pageable);
        }

        Page<ArticleListResponse> responsePage = articles.map(mapper::toListResponse);
        return ApiResponse.success("Articles récupérés avec succès", responsePage);
    }

    @Transactional//(readOnly = true)
    public ApiResponse<ArticleResponse> getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé"));

        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new BusinessException("Cet article n'est pas publié");
        }

        // Incrémenter le compteur de vues
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);

        return ApiResponse.success("Article récupéré", mapper.toResponse(article));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Transactional//(readOnly = true)
    public ApiResponse<Page<ArticleAdminListResponse>> getArticlesForAdmin(
            Pageable pageable,
            String currentUsername,
            boolean isAdmin,
            ArticleStatus status,
            Long categoryId,
            Boolean featured) {

        Specification<Article> spec = (root, query, cb) -> null;

        if (!isAdmin) {
            spec = spec.and(ArticleSpecification.withAuthor(currentUsername));
        }

        if (status != null) {
            spec = spec.and(ArticleSpecification.withStatus(status));
        }

        if (categoryId != null) {
            spec = spec.and(ArticleSpecification.withCategory(categoryId));
        }

        if (featured != null) {
            spec = spec.and(ArticleSpecification.withFeatured(featured));
        }

        Page<Article> articles = articleRepository.findAll(spec, pageable);

        Page<ArticleAdminListResponse> responsePage = articles.map(mapper::toAdminListResponse);

        return ApiResponse.success("Articles récupérés avec succès", responsePage);
    }

    public ApiResponse<Page<ArticleAdminListResponse>> getAllArticlesForAdmin(Pageable pageable) {
        Page<Article> articles = articleRepository.findAll(pageable);
        // Mapper vers ArticleAdminListResponse
        return ApiResponse.success("Liste des articles admin", articles.map(mapper::toAdminListResponse));
    }

    public ApiResponse<ArticleResponse> getArticleForAdmin(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé"));
        return ApiResponse.success("Article récupéré", mapper.toResponse(article));
    }

    public ApiResponse<Void> deleteArticle(Long id, String username) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé"));

        // Vérification droits
        if (!article.getAuthor().getUsername().equals(username) &&
                !hasAdminRole()) {
            throw new BusinessException("Droits insuffisants");
        }

        articleRepository.delete(article);
        return ApiResponse.success("Article supprimé avec succès", null);
    }

    @Transactional//(readOnly = true)
    public ApiResponse<Page<ArticleAdminListResponse>> getMyArticles(
            Pageable pageable,
            String username,
            ArticleStatus status) {

        // Construction dynamique de la requête avec Specification
        Specification<Article> spec = ArticleSpecification.withAuthor(username);

        if (status != null) {
            spec = spec.and(ArticleSpecification.withStatus(status));
        }

        if (status != null) {
            spec = spec.and(ArticleSpecification.withStatus(status));
        }

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("createdAt").descending()
            );
        }

        Page<Article> articles = articleRepository.findAll(spec, pageable);

        Page<ArticleAdminListResponse> responsePage = articles.map(mapper::toAdminListResponse);

        return ApiResponse.success("Mes articles récupérés avec succès", responsePage);
    }

    // Méthodes utilitaires privées
    private Set<Tag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return new HashSet<>();

        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            Tag tag = tagRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> tagRepository.save(new Tag(null, name, mapper.generateSlug(name), new HashSet<>())));
            tags.add(tag);
        }
        return tags;
    }

    private String uploadImage(MultipartFile file, String type) {
        try {
            return minioService.uploadImage(file, "articles/" + type);
        } catch (Exception e) {
            throw new BusinessException("Échec de l'upload de l'image : " + e.getMessage());
        }
    }

    // Autres méthodes : update, publish, delete, findBySlug, findAllPublished, search, etc.

    //FOR PUBLIC
    @Transactional//(readOnly = true)
    public HomePageResponse getHomePageData() {
        return HomePageResponse.builder()
                .featured(getFeaturedArticle())
                .breaking(getBreakingNews(5))
                .latest(getLatestArticles(8))
                .popular(getPopularArticles(6))
                .categories(getPopularCategories())
                .build();
    }

    @Transactional//(readOnly = true)
    public Page<ArticleListHomeResponse> getArticlesByCategory(String categorySlug, Pageable pageable) {
        Page<Article> articles = articleRepository.findByStatusAndCategory_SlugOrderByPublishedAtDesc(
                ArticleStatus.PUBLISHED, categorySlug, pageable);

        return articles.map(mapper::toListHomeResponse);
    }

    @Transactional//(readOnly = true)
    public ArticleResponse getPublicArticleBySlug(String slug) {
        Article article = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé ou non publié"));

        // Incrémenter le compteur de vues
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);

        return mapper.toResponse(article);
    }

    @Transactional//(readOnly = true)
    public Page<ArticleListHomeResponse> searchArticles(String query, Pageable pageable) {
        Specification<Article> spec = Specification.allOf(ArticleSpecification.search(query))
                .and(ArticleSpecification.withStatus(ArticleStatus.PUBLISHED));

        Page<Article> articles = articleRepository.findAll(spec, pageable);
        return articles.map(mapper::toListHomeResponse);
    }

    // Méthode déjà vue précédemment (pour cohérence)
    public List<ArticleListHomeResponse> getBreakingNews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findTopByStatusAndBreakingTrueOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, pageable)
                .stream()
                .map(mapper::toListHomeResponse)
                .toList();
    }

    public List<ArticleListHomeResponse> getPopularArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("viewCount").descending());
        return articleRepository.findByStatusOrderByViewCountDesc(ArticleStatus.PUBLISHED, pageable)
                .getContent()
                .stream()
                .map(mapper::toListHomeResponse)
                .toList();
    }

    private ArticleListHomeResponse getFeaturedArticle() {
        return articleRepository.findTopByStatusAndFeaturedTrueOrderByPublishedAtDesc(ArticleStatus.PUBLISHED)
                .map(mapper::toListHomeResponse)
                .orElse(null);
    }
//
//    public List<ArticleListHomeResponse> getBreakingNews(int limit) {
//        return articleRepository.findTopByStatusAndBreakingTrueOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, limit)
//                .stream()
//                .map(mapper::toListHomeResponse)
//                .toList();
//    }
//
    private List<ArticleListHomeResponse> getLatestArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("publishedAt").descending());
        return articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, pageable)
                .getContent()
                .stream()
                .map(mapper::toListHomeResponse)
                .toList();
    }
//
//    public List<ArticleListHomeResponse> getPopularArticles(int limit) {
//        Pageable pageable = PageRequest.of(0, limit, Sort.by("viewCount").descending());
//        return articleRepository.findByStatusOrderByViewCountDesc(ArticleStatus.PUBLISHED, pageable)
//                .getContent()
//                .stream()
//                .map(mapper::toListHomeResponse)
//                .toList();
//    }
//
    private List<CategoryListResponse> getPopularCategories() {
        // Tu peux faire une requête plus intelligente plus tard
        return categoryRepository.findAll().stream()
                .limit(8)
                .map(c -> new CategoryListResponse(c.getId(), c.getName(), c.getSlug()))
                .toList();
    }

    private boolean hasAdminRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private String generateUniqueSlug(String title, Long articleId) {
        String baseSlug = mapper.generateSlug(title);
        String slug = baseSlug;
        int counter = 1;

        while (articleRepository.existsBySlugAndIdNot(slug, articleId != null ? articleId : 0L)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }


}