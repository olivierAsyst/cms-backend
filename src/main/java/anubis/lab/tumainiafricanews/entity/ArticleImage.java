package anubis.lab.tumainiafricanews.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleImage {
    private String fileName;
    private String url;           // URL MinIO
    private String altText;
    private Integer position;     // Ordre d'affichage
    @ManyToOne(fetch = FetchType.LAZY)
    private Article article;

    public ArticleImage(String originalFilename, String url, String s, int i) {
        this.fileName = originalFilename;
        this.url = url;
        this.altText = s;
        this.position = i;
    }
}