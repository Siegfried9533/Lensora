package com.camerashop.repository;

import com.camerashop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCase(String searchQuery, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCaseAndCategory_CategoryId(
            String searchQuery,
            String categoryId,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.user.userId = :userId")
    List<Product> findByUserId(@Param("userId") String userId);
}
