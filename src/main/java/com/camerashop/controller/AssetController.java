package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.AssetDTO;
import com.camerashop.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AssetDTO> assets = assetService.getAllAssets(pageable);
        return ResponseEntity.ok(ApiResponse.success(assets));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchAssets(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AssetDTO> assets = assetService.searchAssets(searchQuery, categoryId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(assets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAssetById(@PathVariable String id) {
        try {
            AssetDTO asset = assetService.getAssetById(id);
            return ResponseEntity.ok(ApiResponse.success(asset));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse> getAssetsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AssetDTO> assets = assetService.getAssetsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(assets));
    }
}
