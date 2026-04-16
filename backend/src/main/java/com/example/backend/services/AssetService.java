package com.example.backend.services;

import com.example.backend.dto.AssetRequest;
import com.example.backend.dto.AssetResponse;
import com.example.backend.entity.Asset;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ImageRepository;
import jakarta.transaction.Transactional;
import com.example.backend.entity.Category;
import com.example.backend.entity.Image;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
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
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String url : request.getImageUrls()) {
                Image img = new Image();
                img.setUrl(url);
                img.setEntityId(savedAsset.getAssetId());
                img.setType("ASSET"); // Đánh dấu đây là ảnh của Asset
                imageRepository.save(img);
            }
        }
        return getAssetById(savedAsset.getAssetId());
    }

    public AssetResponse getAssetById(@NonNull Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài sản với ID: " + id));

        // 2. Lấy danh sách ảnh từ bảng Images dựa trên ID và loại là ASSET
        List<String> imageUrls = imageRepository.findByEntityIdAndType(id, "ASSET")
                .stream()
                .map(Image::getUrl)
                .toList();

        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .modelName(asset.getModelName())
                .brand(asset.getBrand())
                .description(asset.getDescription())
                .dailyRate(asset.getDailyRate())
                .depositValue(asset.getDepositValue())
                .status(asset.getStatus())
                .categoryName(asset.getCategory().getCategoryName())
                .imageUrls(imageUrls)
                .build();
    }

    public List<AssetResponse> getAllAvailableAssets() {
        List<Asset> assets = assetRepository.findByStatus("AVAILABLE");
        return assets.stream().map(asset -> {
            List<String> imageUrls = imageRepository.findByEntityIdAndType(asset.getAssetId(), "ASSET")
                    .stream()
                    .map(Image::getUrl)
                    .toList();

            return AssetResponse.builder()
                    .assetId(asset.getAssetId())
                    .modelName(asset.getModelName())
                    .brand(asset.getBrand())
                    .description(asset.getDescription())
                    .dailyRate(asset.getDailyRate())
                    .depositValue(asset.getDepositValue())
                    .status(asset.getStatus())
                    .categoryName(asset.getCategory().getCategoryName())
                    .imageUrls(imageUrls)
                    .build();
        }).toList();
    }
}
