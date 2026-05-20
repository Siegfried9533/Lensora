package com.camerashop.repository;

import com.camerashop.entity.Category;
import com.camerashop.entity.Category.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByType(EntityType type);
}
