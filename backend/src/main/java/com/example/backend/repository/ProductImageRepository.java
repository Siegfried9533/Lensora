package com.example.backend.repository;

import com.example.backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId")
    List<ProductImage> findByProductId(@Param("productId") String productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId AND pi.isPrimary = true")
    ProductImage findByProductIdAndIsPrimaryTrue(@Param("productId") String productId);
}
