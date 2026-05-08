package anubis.lab.tumainiafricanews.controller;

import anubis.lab.tumainiafricanews.dto.article.response.CategoryListResponse;
import anubis.lab.tumainiafricanews.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/public/list")
    public List<CategoryListResponse> getOneForAdmin(@PathVariable Long id) {
        return categoryService.getCategories();
    }

}
