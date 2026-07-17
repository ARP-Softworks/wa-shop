package uy.washop.category.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.category.api.dto.CategoryResponse;
import uy.washop.category.api.mapper.CategoryMapper;
import uy.washop.category.infrastructure.CategoryRepository;

@RestController
@RequestMapping("/api/public/categories")
public class PublicCategoryController {

    private final CategoryRepository categoryRepository;

    public PublicCategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }
}
