package anubis.lab.tumainiafricanews.entity;

import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "articles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String slug;                    // Pour SEO : mon-super-article

    @Column(columnDefinition = "TEXT")
    private String summary;                 // Chapô / résumé

    @Column(columnDefinition = "LONGTEXT")
    private String content;                 // Contenu complet (HTML ou Markdown)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "article_images", joinColumns = @JoinColumn(name = "article_id"))
    private List<ArticleImage> images = new ArrayList<>();

    private String mainImageUrl;            // Image principale (featured)

    @Enumerated(EnumType.STRING)
    private ArticleStatus status = ArticleStatus.DRAFT;

    private LocalDateTime publishedAt;
    private LocalDateTime lastModifiedAt;

    private int viewCount = 0;
    private boolean featured = false;
    private boolean breaking = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Méthodes utilitaires
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getArticles().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getArticles().remove(this);
    }

    public void addImage(ArticleImage image) {
        this.images.add(image);
        image.setArticle(this);
    }
}