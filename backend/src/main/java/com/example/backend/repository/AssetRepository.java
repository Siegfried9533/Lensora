package com.example.backend.repository;

import com.example.backend.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    // Tìm kiếm tài sản theo trạng thái máy
    List<Asset> findByStatus(String status);

    // Tìm tài sản theo id
    Asset findByAssetId(Long assetId);

    // Tìm kiếm tài sản theo tên model hoặc thương hiệu (Search bar)
    List<Asset> findByModelNameContainingIgnoreCaseOrBrandContainingIgnoreCase(String modelName, String brand);
}
