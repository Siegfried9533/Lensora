package com.camerashop.repository;

import com.camerashop.entity.Asset;
import com.camerashop.entity.Asset.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {
    @Query("SELECT a FROM Asset a WHERE a.category.categoryId = :categoryId")
    Page<Asset> findByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE " +
           "(:searchQuery IS NULL OR LOWER(a.modelName) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) AND " +
           "(:categoryId IS NULL OR a.category.categoryId = :categoryId) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Asset> searchAssets(@Param("searchQuery") String searchQuery,
                              @Param("categoryId") String categoryId,
                              @Param("status") AssetStatus status,
                              Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.user.userId = :userId")
    List<Asset> findByUserId(@Param("userId") String userId);
}
