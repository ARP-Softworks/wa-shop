package uy.washop.category.api.mapper;

import uy.washop.category.api.dto.CategoryResponse;
import uy.washop.category.domain.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.isActive(),
                category.getSeoTitle(),
                category.getMetaDescription(),
                category.isIndexable(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
