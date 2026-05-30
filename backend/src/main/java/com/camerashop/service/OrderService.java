package com.camerashop.service;

import com.camerashop.dto.OrderDTO;
import com.camerashop.entity.*;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private NotificationService notificationService;

    @SuppressWarnings("unchecked")
    @Transactional
    public OrderDTO createOrder(String email, String shippingAddress, String paymentMethod,
                                 Long shippingFee, List<Map<String, Object>> items) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }

        Order.PaymentMethod orderPaymentMethod = Order.PaymentMethod.valueOf(paymentMethod.toUpperCase());

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .shippingAddress(shippingAddress)
                .status(Order.OrderStatus.PENDING)
                .paymentMethod(orderPaymentMethod)
                .paymentStatus("PENDING")
                .shippingFee(shippingFee != null ? shippingFee : 0L)
                .totalAmount(0L)
                .build();

        long totalAmount = 0;

        for (Map<String, Object> item : items) {
            String productId = (String) item.get("productId");
            int quantity = ((Number) item.get("quantity")).intValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + productId));

            long itemTotal = product.getPrice() * quantity;
            totalAmount += itemTotal;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .priceAtPurchase(product.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);

            // Cap nhat ton kho
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);
        }

        // Them phi van chuyen vao tong
        if (shippingFee != null) {
            totalAmount += shippingFee;
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        // Xoa gio hang cua nguoi dung
        cartItemRepository.deleteByUserId(user.getUserId());

        return toDTO(order);
    }

    public List<OrderDTO> getOrdersByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return orderRepository.findByUserId(user.getUserId(), org.springframework.data.domain.PageRequest.of(0, 100))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return toDTO(order);
    }

    /**
     * Cap nhat trang thai don hang va kich hoat thong bao
     */
    @Transactional
    public OrderDTO updateOrderStatus(String orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // Xu ly logic theo tung trang thai
        if (newStatus == Order.OrderStatus.SHIPPED) {
            order.setShippedDate(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setDeliveredDate(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.CANCELLED) {
            order.setCancelledDate(LocalDateTime.now());
            // Hoan tra ton kho cho don hang bi huy
            order.getOrderItems().forEach(item -> {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            });
        }

        orderRepository.save(order);

        // Gui thong bao thay doi trang thai
        try {
            notificationService.notifyOrderStatusChange(order, oldStatus.name(), newStatus.name());
        } catch (Exception e) {
            System.err.println("Failed to send notification for order status change: " + e.getMessage());
        }

        return toDTO(order);
    }

    private OrderDTO toDTO(Order order) {
        List<OrderDTO.OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> {
                    String imageUrl = null;
                    ProductImage img = productImageRepository.findByProductIdAndIsPrimaryTrue(item.getProduct().getProductId());
                    if (img != null) {
                        imageUrl = img.getUrl();
                    }

                    return OrderDTO.OrderItemDTO.builder()
                            .productName(item.getProduct().getProductName())
                            .quantity(item.getQuantity())
                            .priceAtPurchase(item.getPriceAtPurchase())
                            .imageUrl(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD")
                .paymentStatus(order.getPaymentStatus())
                .shippingFee(order.getShippingFee())
                .ghnOrderId(order.getGhnOrderId())
                .orderItems(itemDTOs)
                .build();
    }
}
