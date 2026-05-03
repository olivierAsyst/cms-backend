package anubis.lab.tumainiafricanews.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String url;
    private String altText;
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    public ArticleImage(String originalFilename, String url, String s, int i) {
        this.fileName = originalFilename;
        this.url = url;
        this.altText = s;
        this.position = i;
    }
}