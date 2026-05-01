package anubis.lab.tumainiafricanews.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String slug;
    private String description;

    @ManyToOne
    private Category parent;   // Pour catégories imbriquées

    @OneToMany(mappedBy = "category")
    private Set<Article> articles = new HashSet<>();
}
