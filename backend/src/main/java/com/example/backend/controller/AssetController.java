package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AssetRequest;
import com.example.backend.dto.AssetResponse;
import com.example.backend.services.AssetService;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    // xem danh sách máy đang rảnh
    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAvailableAssets() {
        return ResponseEntity.ok(assetService.getAllAvailableAssets());
    }

    // lấy chi tiết 1 máy
    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAssetById(id));
    }

    // Admin thêm máy mới
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetResponse> createAsset(@RequestBody AssetRequest request) {
        return ResponseEntity.ok(assetService.createAsset(request));
    }
}
