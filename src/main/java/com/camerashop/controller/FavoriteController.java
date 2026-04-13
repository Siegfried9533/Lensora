package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.FavoriteDTO;
import com.camerashop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse> getFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        List<FavoriteDTO> favorites = favoriteService.getFavorites(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse> toggleFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        try {
            String itemId = body.get("itemId");
            String type = body.get("type");

            FavoriteDTO result = favoriteService.toggleFavorite(userDetails.getUsername(), itemId, type);

            if (result == null) {
                return ResponseEntity.ok(ApiResponse.success(Map.of("action", "removed", "message", "Removed from favorites")));
            } else {
                return ResponseEntity.ok(ApiResponse.success(Map.of("action", "added", "favorite", result)));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse> isFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String itemId,
            @RequestParam String type) {
        boolean isFavorite = favoriteService.isFavorite(userDetails.getUsername(), itemId, type);
        return ResponseEntity.ok(ApiResponse.success(Map.of("isFavorite", isFavorite)));
    }
}
