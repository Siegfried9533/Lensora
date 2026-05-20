package com.camerashop.service;

import com.camerashop.dto.CategoryDTO;
import com.camerashop.entity.Category;
import com.camerashop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getCategoriesByType(String type) {
        Category.EntityType entityType = Category.EntityType.valueOf(type.toUpperCase());
        return categoryRepository.findByType(entityType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .type(category.getType().name())
                .build();
    }
}
