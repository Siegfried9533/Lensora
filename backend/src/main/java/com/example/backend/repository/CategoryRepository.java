package com.example.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // tìm kiếm theo CategoryId
    Optional<Category> findById(Long categoryId);
}
