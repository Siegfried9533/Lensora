package com.example.backend.repository;

import com.example.backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    @Query("SELECT f FROM Favorite f WHERE f.user.userId = :userId")
    List<Favorite> findByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(f) > 0 FROM Favorite f WHERE f.user.userId = :userId AND f.product.productId = :productId")
    boolean existsByUserIdAndProductId(@Param("userId") String userId, @Param("productId") String productId);

    @Query("SELECT COUNT(f) > 0 FROM Favorite f WHERE f.user.userId = :userId AND f.asset.assetId = :assetId")
    boolean existsByUserIdAndAssetId(@Param("userId") String userId, @Param("assetId") String assetId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.userId = :userId AND f.product.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") String userId, @Param("productId") String productId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.userId = :userId AND f.asset.assetId = :assetId")
    void deleteByUserIdAndAssetId(@Param("userId") String userId, @Param("assetId") String assetId);
}
