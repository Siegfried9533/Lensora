package com.camerashop.service;

import com.camerashop.dto.AssetDTO;
import com.camerashop.entity.Asset;
import com.camerashop.entity.AssetImage;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.AssetRepository;
import com.camerashop.repository.AssetImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetImageRepository assetImageRepository;

    public Page<AssetDTO> getAllAssets(Pageable pageable) {
        return assetRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<AssetDTO> getAssetsByCategory(String categoryId, Pageable pageable) {
        return assetRepository.findByCategoryId(categoryId, pageable).map(this::toDTO);
    }

    public Page<AssetDTO> searchAssets(String searchQuery, String categoryId, String status, Pageable pageable) {
        String normalizedSearch = normalizeFilter(searchQuery);
        String normalizedCategoryId = normalizeFilter(categoryId);
        String normalizedStatus = normalizeFilter(status);
        Asset.AssetStatus assetStatus = normalizedStatus != null ? Asset.AssetStatus.valueOf(normalizedStatus) : null;

        if (normalizedSearch == null && normalizedCategoryId == null && assetStatus == null) {
            return assetRepository.findAll(pageable).map(this::toDTO);
        }

        if (normalizedSearch == null && normalizedCategoryId == null) {
            return assetRepository.findByStatus(assetStatus, pageable).map(this::toDTO);
        }

        if (normalizedSearch == null && assetStatus == null) {
            return assetRepository.findByCategoryId(normalizedCategoryId, pageable).map(this::toDTO);
        }

        if (normalizedSearch == null) {
            return assetRepository.findByCategory_CategoryIdAndStatus(normalizedCategoryId, assetStatus, pageable)
                    .map(this::toDTO);
        }

        if (normalizedCategoryId == null && assetStatus == null) {
            return assetRepository.findByModelNameContainingIgnoreCase(normalizedSearch, pageable).map(this::toDTO);
        }

        if (normalizedCategoryId == null) {
            return assetRepository.findByModelNameContainingIgnoreCaseAndStatus(
                    normalizedSearch,
                    assetStatus,
                    pageable).map(this::toDTO);
        }

        if (assetStatus == null) {
            return assetRepository.findByModelNameContainingIgnoreCaseAndCategory_CategoryId(
                    normalizedSearch,
                    normalizedCategoryId,
                    pageable).map(this::toDTO);
        }

        return assetRepository.findByModelNameContainingIgnoreCaseAndCategory_CategoryIdAndStatus(
                normalizedSearch,
                normalizedCategoryId,
                assetStatus,
                pageable).map(this::toDTO);
    }

    public AssetDTO getAssetById(String id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị cho thuê"));
        return toDTO(asset);
    }

    public List<AssetDTO> getAssetsByUser(String userId) {
        return assetRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private AssetDTO toDTO(Asset asset) {
        List<AssetImage> images = assetImageRepository.findByAssetId(asset.getAssetId());
        String primaryImageUrl = images.stream()
                .filter(AssetImage::getIsPrimary)
                .findFirst()
                .map(AssetImage::getUrl)
                .orElse("https://via.placeholder.com/800");

        List<String> imageUrls = images.stream()
                .map(AssetImage::getUrl)
                .collect(Collectors.toList());

        return AssetDTO.builder()
                .assetId(asset.getAssetId())
                .categoryId(asset.getCategory().getCategoryId())
                .categoryName(asset.getCategory().getCategoryName())
                .userId(asset.getUser().getUserId())
                .modelName(asset.getModelName())
                .brand(asset.getBrand())
                .dailyRate(asset.getDailyRate())
                .status(asset.getStatus().name())
                .serialNumber(asset.getSerialNumber())
                .imageUrls(imageUrls)
                .primaryImageUrl(primaryImageUrl)
                .build();
    }
}
