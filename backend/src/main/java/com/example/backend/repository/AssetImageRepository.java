package com.example.backend.repository;

import com.example.backend.entity.AssetImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetImageRepository extends JpaRepository<AssetImage, Long> {
    @Query("SELECT ai FROM AssetImage ai WHERE ai.assetId = :assetId")
    List<AssetImage> findByAssetId(@Param("assetId") Long assetId);

    @Query("SELECT ai FROM AssetImage ai WHERE ai.assetId = :assetId AND ai.isPrimary = true")
    AssetImage findByAssetIdAndIsPrimaryTrue(@Param("assetId") Long assetId);
}
