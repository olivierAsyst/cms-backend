package anubis.lab.tumainiafricanews.repository;

import anubis.lab.tumainiafricanews.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByNameIgnoreCase(String name);

    List<Category> findByParentIsNullOrderByName(); // Catégories racines
}
