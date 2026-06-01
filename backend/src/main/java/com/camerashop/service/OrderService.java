package com.camerashop.service;

import com.camerashop.dto.OrderDTO;
import com.camerashop.entity.*;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    /** Khoi luong mac dinh moi san pham (gram) khi san pham chua khai bao can nang. */
    private static final int DEFAULT_ITEM_WEIGHT_GRAMS = 500;
    /** GHN rejects declared insurance values above this limit. */
    private static final long MAX_GHN_INSURANCE_VALUE = 5_000_000L;
    /** Test COD amount sent to GHN so high-value demo orders are accepted. */
    private static final long DEFAULT_GHN_COD_AMOUNT = 10_000L;

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

    @Autowired
    private GHNService ghnService;

    @SuppressWarnings("unchecked")
    @Transactional
    public OrderDTO createOrder(String email, String shippingAddress, String paymentMethod,
                                 Long shippingFee, List<Map<String, Object>> items, boolean clearCart,
                                 String recipientName, String recipientPhone,
                                 String toDistrictId, String toWardCode) {
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

        if (clearCart) {
            cartItemRepository.deleteByUserId(user.getUserId());
        }

        String ghnWarning = createGhnShippingOrder(order, recipientName, recipientPhone, toDistrictId, toWardCode);

        OrderDTO dto = toDTO(order);
        dto.setGhnWarning(ghnWarning);
        return dto;
    }

    /**
     * Tao van don GHN cho don hang vua tao. Loi GHN khong lam fail don hang noi bo:
     * tra ve thong diep canh bao (hoac null neu thanh cong / da bo qua).
     */
    private String createGhnShippingOrder(Order order, String recipientName, String recipientPhone,
                                          String toDistrictId, String toWardCode) {
        if (!ghnService.isConfigured()) {
            log.warn("GHN chưa được cấu hình - bỏ qua tạo vận đơn cho đơn {}", order.getOrderId());
            return null;
        }
        try {
            int totalWeight = 0;
            List<Map<String, Object>> ghnItems = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                totalWeight += qty * DEFAULT_ITEM_WEIGHT_GRAMS;
                Map<String, Object> ghnItem = new HashMap<>();
                ghnItem.put("name", item.getProduct().getProductName());
                ghnItem.put("quantity", qty);
                ghnItem.put("weight", DEFAULT_ITEM_WEIGHT_GRAMS);
                ghnItems.add(ghnItem);
            }

            long goodsValue = order.getTotalAmount()
                    - (order.getShippingFee() != null ? order.getShippingFee() : 0L);
            long codAmount = order.getPaymentMethod() == Order.PaymentMethod.COD
                    ? DEFAULT_GHN_COD_AMOUNT : 0L;

            String ghnOrderCode = ghnService.createShippingOrder(
                    order.getOrderId(),
                    recipientName,
                    recipientPhone,
                    order.getShippingAddress(),
                    toWardCode,
                    toDistrictId,
                    totalWeight,
                    Math.min(MAX_GHN_INSURANCE_VALUE, Math.max(0, goodsValue)),
                    codAmount,
                    ghnItems);

            order.setGhnOrderId(ghnOrderCode);
            orderRepository.save(order);
            log.info("Created GHN shipping order {} for order {}", ghnOrderCode, order.getOrderId());
            return null;

        } catch (Exception e) {
            log.error("Không thể tạo vận đơn GHN cho đơn {}: {}", order.getOrderId(), e.getMessage());
            return "Đơn hàng đã được tạo nhưng chưa tạo được vận đơn GHN: " + e.getMessage();
        }
    }

    public List<OrderDTO> getOrdersByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return orderRepository.findByUserId(
                        user.getUserId(),
                        PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "orderDate")))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Trạng thái kết thúc — không đồng bộ thêm từ GHN nữa. */
    private static final List<Order.OrderStatus> TERMINAL_STATUSES =
            List.of(Order.OrderStatus.CANCELLED, Order.OrderStatus.DELIVERED);

    public OrderDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        // Không gọi GHN tại đây: job quét định kỳ (syncAllGhnOrders) là nơi DUY NHẤT ghi trạng thái
        // từ GHN, tránh race ghi đè/hoàn kho 2 lần và độ trễ HTTP mỗi lần mở chi tiết. Màn hình
        // giao dịch poll danh sách nội bộ nên vẫn phản ánh thay đổi trong vòng ~30s.
        return toDTO(order);
    }

    @Transactional
    public OrderDTO cancelOrderForCustomer(String email, String orderId, String cancelReason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("Không có quyền truy cập");
        }
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng đang chờ xử lý");
        }

        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED, cancelReason);
    }

    @Transactional
    public OrderDTO updateOrderStatus(String orderId, Order.OrderStatus newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }

    /**
     * Cap nhat trang thai don hang va kich hoat thong bao.
     * Khi huy (CANCELLED) co the kem ly do va se day lenh huy sang GHN.
     */
    @Transactional
    public OrderDTO updateOrderStatus(String orderId, Order.OrderStatus newStatus, String cancelReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return applyStatus(order, newStatus, true, cancelReason);
    }

    /**
     * Áp dụng trạng thái mới cho đơn và gửi thông báo.
     *
     * @param propagateToGhn true khi thay đổi bắt nguồn từ app (khách/admin hủy) → gọi GHN hủy vận đơn;
     *                       false khi GHN là nguồn thay đổi → KHÔNG gọi lại GHN để tránh vòng lặp.
     * @param cancelReason   lý do hủy (chỉ dùng khi newStatus = CANCELLED).
     */
    private OrderDTO applyStatus(Order order, Order.OrderStatus newStatus,
                                 boolean propagateToGhn, String cancelReason) {
        Order.OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) {
            return toDTO(order);
        }
        order.setStatus(newStatus);

        // Xu ly logic theo tung trang thai
        if (newStatus == Order.OrderStatus.SHIPPED) {
            order.setShippedDate(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setDeliveredDate(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.CANCELLED) {
            order.setCancelledDate(LocalDateTime.now());
            if (cancelReason != null && !cancelReason.isBlank()) {
                order.setCancelReason(cancelReason);
            }
            // Huy van don GHN (neu app la nguon huy); loi GHN khong chan viec huy don noi bo
            if (propagateToGhn && order.getGhnOrderId() != null && !order.getGhnOrderId().isBlank()
                    && ghnService.isConfigured()) {
                try {
                    ghnService.cancelShippingOrder(order.getGhnOrderId());
                } catch (Exception e) {
                    log.error("Không thể hủy vận đơn GHN {} cho đơn {}: {}",
                            order.getGhnOrderId(), order.getOrderId(), e.getMessage());
                }
            }
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

    /**
     * Quét tất cả đơn còn mở có mã vận đơn GHN và đồng bộ trạng thái từ GHN về app.
     * Gọi bởi job định kỳ ({@code NotificationScheduler}) — tách lời gọi GHN ra khỏi
     * đường đọc danh sách để giao diện vẫn nhanh khi poll. Trả về số đơn thay đổi.
     */
    @Transactional
    public int syncAllGhnOrders() {
        if (!ghnService.isConfigured()) {
            return 0;
        }
        int changed = 0;
        for (Order order : orderRepository.findSyncableGhnOrders(TERMINAL_STATUSES)) {
            if (syncOrderFromGhn(order)) {
                changed++;
            }
        }
        return changed;
    }

    /**
     * Đồng bộ 1 đơn từ GHN. Trả về true nếu trạng thái nội bộ thay đổi.
     * GHN lỗi / trạng thái không xác định ("UNKNOWN") = không làm gì (giữ nguyên trạng thái nội bộ),
     * nên một sự cố GHN tạm thời không ghi đè trạng thái thật.
     */
    @Transactional
    public boolean syncOrderFromGhn(Order order) {
        if (!ghnService.isConfigured()) {
            return false;
        }
        String code = order.getGhnOrderId();
        if (code == null || code.isBlank()) {
            return false;
        }
        Order.OrderStatus current = order.getStatus();
        if (current == Order.OrderStatus.CANCELLED || current == Order.OrderStatus.DELIVERED) {
            return false;
        }
        Order.OrderStatus mapped = mapGhnStatus(ghnService.getOrderStatus(code));
        if (mapped == null || mapped == current) {
            return false;
        }
        String reason = mapped == Order.OrderStatus.CANCELLED ? "Vận đơn đã bị hủy trên GHN" : null;
        // GHN là nguồn thay đổi → propagateToGhn = false (không gọi lại lệnh hủy GHN).
        applyStatus(order, mapped, false, reason);
        return true;
    }

    /** Ánh xạ trạng thái vận đơn GHN v2 sang trạng thái đơn nội bộ. null = bỏ qua (giữ nguyên). */
    private Order.OrderStatus mapGhnStatus(String ghnStatus) {
        if (ghnStatus == null) {
            return null;
        }
        switch (ghnStatus.toLowerCase()) {
            case "cancel":
                return Order.OrderStatus.CANCELLED;
            case "delivered":
                return Order.OrderStatus.DELIVERED;
            case "delivering":
            case "money_collect_delivering":
                return Order.OrderStatus.SHIPPED;
            default:
                // ready_to_pick, picking, picked, storing, transporting, sorting, UNKNOWN... → giữ nguyên
                return null;
        }
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
                .cancelReason(order.getCancelReason())
                .orderItems(itemDTOs)
                .build();
    }
}
