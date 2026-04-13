package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.CartItemDTO;
import com.camerashop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse> getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        // Get user email, then find user by email to get userId
        // For now, we'll use a simpler approach - the email is the identifier
        List<CartItemDTO> cartItems = cartService.getCartItems(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(cartItems));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            String itemId = (String) body.get("itemId");
            String type = (String) body.get("type");
            Integer quantity = body.get("quantity") != null ?
                ((Number) body.get("quantity")).intValue() : 1;

            CartItemDTO cartItem = cartService.addToCart(userDetails.getUsername(), itemId, type, quantity);
            return ResponseEntity.ok(ApiResponse.success(cartItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable String id) {
        try {
            cartService.removeFromCart(id);
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<ApiResponse> updateQuantity(
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        try {
            CartItemDTO cartItem = cartService.updateQuantity(id, body.get("quantity"));
            return ResponseEntity.ok(ApiResponse.success(cartItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
