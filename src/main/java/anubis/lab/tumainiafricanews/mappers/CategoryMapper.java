package anubis.lab.tumainiafricanews.mappers;

import anubis.lab.tumainiafricanews.dto.article.response.CategoryListResponse;
import anubis.lab.tumainiafricanews.entity.Category;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {

    public CategoryListResponse toList(Category category){
        if (category == null) {
            return null;
        }
        return new CategoryListResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }
}
