package anubis.lab.tumainiafricanews.service;

import anubis.lab.tumainiafricanews.dto.article.response.CategoryListResponse;
import anubis.lab.tumainiafricanews.entity.Category;
import anubis.lab.tumainiafricanews.mappers.CategoryMapper;
import anubis.lab.tumainiafricanews.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = Exception.class)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public List<CategoryListResponse> getCategories(){
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(mapper::toList)
                .toList();
    }

}
