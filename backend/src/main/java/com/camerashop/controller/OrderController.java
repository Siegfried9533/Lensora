package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.OrderDTO;
import com.camerashop.entity.Order;
import com.camerashop.entity.User;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.OrderRepository;
import com.camerashop.repository.UserRepository;
import com.camerashop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            String shippingAddress = (String) body.get("shippingAddress");
            String paymentMethod = (String) body.getOrDefault("paymentMethod", "COD");
            Long shippingFee = body.get("shippingFee") != null ? ((Number) body.get("shippingFee")).longValue() : null;
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            OrderDTO order = orderService.createOrder(userDetails.getUsername(), shippingAddress, paymentMethod, shippingFee, items);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getOrdersByUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> orders = orderService.getOrdersByUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền truy cập"));
            }
            OrderDTO orderDTO = orderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.success(orderDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng.
     * ADMIN có thể cập nhật mọi trạng thái.
     * USER chỉ có thể hủy đơn hàng của chính mình.
     * PATCH /api/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Trạng thái là bắt buộc"));
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

            boolean isAdmin = user.getRole() == User.Role.ADMIN;
            boolean isOwner = order.getUser().getUserId().equals(user.getUserId());

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền truy cập"));
            }

            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);

            if (!isAdmin) {
                if (newStatus != Order.OrderStatus.CANCELLED) {
                    return ResponseEntity.status(403).body(ApiResponse.error("Chỉ admin mới có quyền cập nhật trạng thái này"));
                }
                if (order.getStatus() != Order.OrderStatus.PENDING) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ có thể hủy đơn hàng đang ở trạng thái PENDING"));
                }
            }

            OrderDTO result = orderService.updateOrderStatus(orderId, newStatus);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Trạng thái không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Cập nhật trạng thái đơn hàng thất bại: " + e.getMessage()));
        }
    }
}
