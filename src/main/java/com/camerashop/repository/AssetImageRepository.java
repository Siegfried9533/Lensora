package com.camerashop.repository;

import com.camerashop.entity.AssetImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetImageRepository extends JpaRepository<AssetImage, String> {
    @Query("SELECT ai FROM AssetImage ai WHERE ai.asset.assetId = :assetId")
    List<AssetImage> findByAssetId(@Param("assetId") String assetId);

    @Query("SELECT ai FROM AssetImage ai WHERE ai.asset.assetId = :assetId AND ai.isPrimary = true")
    AssetImage findByAssetIdAndIsPrimaryTrue(@Param("assetId") String assetId);
}
