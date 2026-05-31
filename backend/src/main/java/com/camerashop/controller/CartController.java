package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.CartItemDTO;
import com.camerashop.entity.CartItem;
import com.camerashop.entity.User;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.CartItemRepository;
import com.camerashop.repository.UserRepository;
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

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse> getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        // Lay email nguoi dung, sau do tim nguoi dung theo email de lay userId
        // Hien tai se dung cach don gian hon - email la dinh danh
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
    public ResponseEntity<ApiResponse> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));
            if (!cartItem.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền truy cập"));
            }
            cartService.removeFromCart(id);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<ApiResponse> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));
            if (!cartItem.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền truy cập"));
            }
            CartItemDTO updated = cartService.updateQuantity(id, body.get("quantity"));
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa toàn bộ giỏ hàng"));
    }
}
