package com.camerashop.service;

import com.camerashop.dto.AssetDTO;
import com.camerashop.entity.Asset;
import com.camerashop.entity.AssetImage;
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
        Asset.AssetStatus assetStatus = status != null ? Asset.AssetStatus.valueOf(status) : null;
        return assetRepository.searchAssets(searchQuery, categoryId, assetStatus, pageable).map(this::toDTO);
    }

    public AssetDTO getAssetById(String id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        return toDTO(asset);
    }

    public List<AssetDTO> getAssetsByUser(String userId) {
        return assetRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
