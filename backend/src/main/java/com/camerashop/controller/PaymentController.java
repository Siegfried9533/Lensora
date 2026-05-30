package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.entity.Order;
import com.camerashop.entity.PaymentTransaction;
import com.camerashop.entity.Rental;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.OrderRepository;
import com.camerashop.repository.PaymentTransactionRepository;
import com.camerashop.repository.RentalRepository;
import com.camerashop.service.EmailService;
import com.camerashop.service.MoMoService;
import com.camerashop.service.NotificationService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private MoMoService momoService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.frontend-url:http://localhost:8081}")
    private String frontendUrl;

    /**
     * Tạo URL thanh toán MoMo cho đơn hàng
     * POST /api/payment/momo/create
     */
    @PostMapping("/momo/create")
    public ResponseEntity<ApiResponse> createMoMoPayment(@RequestBody Map<String, Object> body) {
        try {
            String orderId = (String) body.get("orderId");
            Long amount = ((Number) body.get("amount")).longValue();
            String orderInfo = body.get("orderInfo") != null ? (String) body.get("orderInfo")
                    : "Thanh toan don hang: " + orderId;
            String requestType = body.get("requestType") != null ? (String) body.get("requestType") : "captureWallet";

            // Kiểm tra đơn hàng tồn tại
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                throw new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId);
            }

            Order order = orderOpt.get();

            // totalAmount đã bao gồm phí vận chuyển (được đặt bởi OrderService)
            long totalAmount = order.getTotalAmount();

            // Kiểm tra số tiền khớp nhau
            if (totalAmount != amount) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Số tiền không khớp. Dự kiến: " + totalAmount));
            }

            // Tạo URL thanh toán MoMo
            MoMoService.RequestType type = MoMoService.RequestType.CAPTURE_WALLET;
            if ("payWithMethod".equals(requestType)) {
                type = MoMoService.RequestType.PAY_WITH_METHOD;
            }

            String payUrl = momoService.createPaymentUrl(orderId, totalAmount, orderInfo, type);

            Map<String, String> response = new HashMap<>();
            response.put("payUrl", payUrl);
            response.put("orderId", orderId);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Yêu cầu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Tạo thanh toán thất bại: " + e.getMessage()));
        }
    }

    /**
     * Tạo URL thanh toán MoMo cho đơn thuê
     * POST /api/payment/momo/create-rental
     */
    @PostMapping("/momo/create-rental")
    public ResponseEntity<ApiResponse> createMoMoPaymentRental(@RequestBody Map<String, Object> body) {
        try {
            String rentalId = (String) body.get("rentalId");
            String orderInfo = body.get("orderInfo") != null ? (String) body.get("orderInfo")
                    : "Thanh toan thue: " + rentalId;

            // Kiểm tra đơn thuê tồn tại
            Optional<Rental> rentalOpt = rentalRepository.findById(rentalId);
            if (rentalOpt.isEmpty()) {
                throw new ResourceNotFoundException("Không tìm thấy đơn thuê: " + rentalId);
            }

            Rental rental = rentalOpt.get();
            long totalAmount = rental.getTotalRentFee() + rental.getDepositFee();
            if (rental.getShippingFee() != null) {
                totalAmount += rental.getShippingFee();
            }

            // Tạo URL thanh toán MoMo
            String payUrl = momoService.createPaymentUrl(rentalId, totalAmount, orderInfo);

            Map<String, String> response = new HashMap<>();
            response.put("payUrl", payUrl);
            response.put("rentalId", rentalId);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Tạo thanh toán thất bại: " + e.getMessage()));
        }
    }

    /**
     * Xử lý callback IPN từ MoMo (server-to-server)
     * POST /api/payment/momo/ipn
     *
     * Endpoint này nhận thông báo bất đồng bộ từ MoMo
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<Map<String, String>> handleMoMoIPN(@RequestBody Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        try {
            // Xác thực chữ ký
            if (!momoService.validateIPNCallback(params)) {
                System.err.println("Invalid MoMo IPN signature");
                response.put("message", "Chữ ký không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            String orderId = params.get("orderId");
            String transId = params.get("transId");
            String resultCode = params.getOrDefault("resultCode", params.getOrDefault("errorCode", ""));
            String amount = params.get("amount");
            String message = params.get("message");

            // Phân tích số tiền
            long paymentAmount = 0;
            if (amount != null && !amount.isEmpty()) {
                paymentAmount = Long.parseLong(amount);
            }

            // Kiểm tra kết quả thanh toán
            boolean isSuccess = "0".equals(resultCode);

            // Lưu giao dịch thanh toán
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .transactionRef(transId)
                    .orderCode(orderId)
                    .amount((double) paymentAmount)
                    .paymentMethod("MoMo")
                    .responseCode(resultCode)
                    .responseMessage(message)
                    .status(isSuccess ? PaymentTransaction.PaymentStatus.SUCCESS
                            : PaymentTransaction.PaymentStatus.FAILED)
                    .build();

            // Kiểm tra xem là đơn hàng hay đơn thuê
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                transaction.setOrder(order);
                paymentTransactionRepository.save(transaction);

                if (isSuccess) {
                    // Thanh toán thành công
                    order.setPaymentStatus("SUCCESS");
                    if (order.getStatus() == Order.OrderStatus.PENDING) {
                        order.setStatus(Order.OrderStatus.SHIPPED);
                    }
                    orderRepository.save(order);

                    // Gửi email xác nhận
                    try {
                        sendOrderConfirmationEmail(order);
                    } catch (MessagingException e) {
                        System.err.println("Failed to send confirmation email: " + e.getMessage());
                    }

                    // Gửi thông báo thanh toán thành công
                    try {
                        notificationService.notifyPaymentSuccess(order, (double) paymentAmount);
                    } catch (Exception e) {
                        System.err.println("Failed to send payment notification: " + e.getMessage());
                    }

                    System.out.println("Order " + orderId + " paid successfully via MoMo");
                } else {
                    order.setPaymentStatus("FAILED");
                    orderRepository.save(order);
                    System.out.println("Order " + orderId + " payment failed: " + message);
                }
            } else {
                Optional<Rental> rentalOpt = rentalRepository.findById(orderId);
                if (rentalOpt.isPresent()) {
                    Rental rental = rentalOpt.get();
                    transaction.setRental(rental);
                    paymentTransactionRepository.save(transaction);

                    if (isSuccess) {
                        rental.setStatus(Rental.RentalStatus.ACTIVE);
                        rentalRepository.save(rental);
                    } else {
                        rental.setStatus(Rental.RentalStatus.CANCELLED);
                        rentalRepository.save(rental);
                    }
                }
            }

            // Phản hồi cho MoMo với trạng thái thành công
            response.put("errorCode", "0");
            response.put("message", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error handling MoMo IPN: " + e.getMessage());
            response.put("errorCode", "99");
            response.put("message", "Lỗi nội bộ: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Xử lý callback redirect từ MoMo (người dùng được chuyển hướng sau thanh toán)
     * GET /api/payment/momo/callback
     *
     * Endpoint này xử lý chuyển hướng người dùng sau khi hoàn tất thanh toán
     */
    @GetMapping("/momo/callback")
    public ResponseEntity<Void> handleMoMoRedirect(@RequestParam Map<String, String> params) {
        try {
            // Xác thực chữ ký
            if (!momoService.validateRedirectCallback(params)) {
                System.err.println("Invalid MoMo redirect signature");
                return ResponseEntity.status(302)
                        .header("Location", frontendUrl + "/payment-failed?error=invalid_signature")
                        .build();
            }

            String orderId = params.get("orderId");
            String errorCode = params.get("errorCode");
            boolean isSuccess = "0".equals(errorCode);

            // Chuyển hướng tới trang thành công/thất bại của frontend
            String redirectUrl = isSuccess
                    ? frontendUrl + "/payment-success?orderCode=" + orderId
                    : frontendUrl + "/payment-failed?orderCode=" + orderId + "&error=" + params.get("message");

            return ResponseEntity.status(302).header("Location", redirectUrl).build();

        } catch (Exception e) {
            System.err.println("Error handling MoMo redirect: " + e.getMessage());
            return ResponseEntity.status(302)
                    .header("Location", frontendUrl + "/payment-failed?error=internal_error")
                    .build();
        }
    }

    /**
     * Lấy trạng thái thanh toán cho đơn hàng
     * GET /api/payment/status/{orderCode}
     */
    @GetMapping("/status/{orderCode}")
    public ResponseEntity<ApiResponse> getPaymentStatus(@PathVariable String orderCode) {
        try {
            Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findByOrderCode(orderCode);

            if (transactionOpt.isPresent()) {
                PaymentTransaction transaction = transactionOpt.get();
                Map<String, Object> result = new HashMap<>();
                result.put("success", transaction.getStatus() == PaymentTransaction.PaymentStatus.SUCCESS);
                result.put("message", transaction.getResponseMessage());
                result.put("orderCode", orderCode);
                result.put("amount", transaction.getAmount());
                result.put("transactionRef", transaction.getTransactionRef());
                result.put("paymentMethod", transaction.getPaymentMethod());
                return ResponseEntity.ok(ApiResponse.success(result));
            }

            // Kiểm tra trạng thái thanh toán đơn hàng
            Optional<Order> orderOpt = orderRepository.findById(orderCode);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                Map<String, Object> result = new HashMap<>();
                result.put("success", "SUCCESS".equals(order.getPaymentStatus()));
                result.put("orderCode", orderCode);
                result.put("amount", (double) order.getTotalAmount());
                result.put("paymentStatus", order.getPaymentStatus());
                return ResponseEntity.ok(ApiResponse.success(result));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Không tìm thấy thanh toán");
            result.put("orderCode", orderCode);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lấy trạng thái thanh toán thất bại: " + e.getMessage()));
        }
    }

    /**
     * Truy vấn trạng thái giao dịch MoMo
     * POST /api/payment/momo/query
     */
    @PostMapping("/momo/query")
    public ResponseEntity<ApiResponse> queryMoMoTransaction(@RequestBody Map<String, String> body) {
        try {
            String orderId = body.get("orderId");
            String requestId = body.get("requestId");

            if (orderId == null || requestId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("orderId và requestId là bắt buộc"));
            }

            Map<String, Object> result = momoService.queryTransaction(orderId, requestId);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Truy vấn giao dịch thất bại: " + e.getMessage()));
        }
    }

    private void sendOrderConfirmationEmail(Order order) throws MessagingException {
        // Lấy email người dùng từ đơn hàng
        String userEmail = order.getUser().getEmail();
        String userName = order.getUser().getUserName();

        // Xây dựng chuỗi chi tiết đơn hàng
        StringBuilder orderDetails = new StringBuilder();
        order.getOrderItems().forEach(item -> {
            orderDetails.append("<p>")
                    .append(item.getProduct().getProductName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append(" - ₫")
                    .append(String.format("%,.0f", item.getPriceAtPurchase() * item.getQuantity()))
                    .append("</p>");
        });

        if (order.getShippingFee() != null && order.getShippingFee() > 0) {
            orderDetails.append("<p>Shipping: ₫").append(String.format("%,.0f", order.getShippingFee())).append("</p>");
        }

        emailService.sendOrderConfirmation(
                userEmail,
                userName,
                order.getOrderId(),
                orderDetails.toString(),
                (double) order.getTotalAmount(),
                order.getPaymentStatus());
    }
}
