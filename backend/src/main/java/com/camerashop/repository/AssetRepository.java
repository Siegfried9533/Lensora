package com.camerashop.repository;

import com.camerashop.entity.Asset;
import com.camerashop.entity.Asset.AssetStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {

    /**
     * Fetch an asset with a row-level write lock. Used when creating a rental so that
     * concurrent rental requests for the same asset are serialized — the overlap
     * re-check then runs while holding the lock, preventing double-booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.assetId = :assetId")
    Optional<Asset> findByIdForUpdate(@Param("assetId") String assetId);
    @Query("SELECT a FROM Asset a WHERE a.category.categoryId = :categoryId")
    Page<Asset> findByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    Page<Asset> findByCategory_CategoryIdAndStatus(String categoryId, AssetStatus status, Pageable pageable);

    Page<Asset> findByModelNameContainingIgnoreCase(String searchQuery, Pageable pageable);

    Page<Asset> findByModelNameContainingIgnoreCaseAndCategory_CategoryId(
            String searchQuery,
            String categoryId,
            Pageable pageable);

    Page<Asset> findByModelNameContainingIgnoreCaseAndStatus(
            String searchQuery,
            AssetStatus status,
            Pageable pageable);

    Page<Asset> findByModelNameContainingIgnoreCaseAndCategory_CategoryIdAndStatus(
            String searchQuery,
            String categoryId,
            AssetStatus status,
            Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.user.userId = :userId")
    List<Asset> findByUserId(@Param("userId") String userId);
}
