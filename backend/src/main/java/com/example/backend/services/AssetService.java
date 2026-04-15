package com.example.backend.services;

import com.example.backend.dto.AssetRequest;
import com.example.backend.dto.AssetResponse;
import com.example.backend.entity.Asset;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ImageRepository;
import jakarta.transaction.Transactional;
import com.example.backend.entity.Category;

public class AssetService {
    private final AssetRepository assetRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public AssetResponse createAsset(AssetRequest request) {
        // tìm category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        // lưu asset
        Asset asset = new Asset();
        asset.setCategory(category);
        asset.setModelName(request.getModelName());
        asset.setBrand(request.getBrand());
        asset.setDescription(request.getDescription());
        asset.setDailyRate(request.getDailyRate());
        asset.setDepositValue(request.getDepositValue());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setStatus("AVAILABLE"); // Mặc định khi tạo mới sẽ là AVAILABLE

        Asset savedAsset = assetRepository.save(asset);

        // lưu image
        if (request.getImageUrls() != null) {
            request.getImageUrls().forEach(url -> {
                imageRepository.save(new com.example.backend.entity.Image(null, url, savedAsset));
            });
        }
        return convertToResponse(savedAsset);
    }
}
