package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.entity.Order;
import com.example.backend.entity.PaymentTransaction;
import com.example.backend.entity.Rental;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentTransactionRepository;
import com.example.backend.repository.RentalRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.MoMoService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @Value("${app.frontend-url:http://localhost:8081}")
    private String frontendUrl;

    /**
     * Create MoMo payment URL for an order
     * POST /api/payment/momo/create
     */
    @PostMapping("/momo/create")
    public ResponseEntity<ApiResponse> createMoMoPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            String orderId = (String) body.get("orderId");
            Number amountNumber = (Number) body.get("amount");
            if (orderId == null || orderId.isBlank() || amountNumber == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("orderId and amount are required"));
            }

            Long amount = amountNumber.longValue();
            if (amount <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("amount must be greater than 0"));
            }

            String orderInfo = body.get("orderInfo") != null ? (String) body.get("orderInfo")
                    : "Thanh toan don hang: " + orderId;
            String requestType = body.get("requestType") != null ? (String) body.get("requestType") : "captureWallet";

            // Verify order exists
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order not found: " + orderId));
            }

            Order order = orderOpt.get();

            if (!isOrderOwnedByCurrentUser(order, userDetails.getUsername())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Not authorized"));
            }

            if ("SUCCESS".equals(order.getPaymentStatus())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order has already been paid"));
            }

            // Calculate total amount (including shipping if applicable)
            long totalAmount = order.getTotalAmount();
            if (order.getShippingFee() != null) {
                totalAmount += order.getShippingFee();
            }

            // Verify amount matches
            if (totalAmount != amount) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Amount mismatch. Expected: " + totalAmount));
            }

            // Create MoMo payment URL
            MoMoService.RequestType type = MoMoService.RequestType.CAPTURE_WALLET;
            if ("payWithMethod".equals(requestType)) {
                type = MoMoService.RequestType.PAY_WITH_METHOD;
            }

            String payUrl = momoService.createPaymentUrl(orderId, totalAmount, orderInfo, type);

            Map<String, String> response = new HashMap<>();
            response.put("payUrl", payUrl);
            response.put("orderId", orderId);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create payment: " + e.getMessage()));
        }
    }

    /**
     * Create MoMo payment URL for a rental
     * POST /api/payment/momo/create-rental
     */
    @PostMapping("/momo/create-rental")
    public ResponseEntity<ApiResponse> createMoMoPaymentRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            String rentalId = (String) body.get("rentalId");
            if (rentalId == null || rentalId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("rentalId is required"));
            }

            String orderInfo = body.get("orderInfo") != null ? (String) body.get("orderInfo")
                    : "Thanh toan thue: " + rentalId;

            // Verify rental exists
            Optional<Rental> rentalOpt = rentalRepository.findById(rentalId);
            if (rentalOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Rental not found: " + rentalId));
            }

            Rental rental = rentalOpt.get();

            if (!isRentalOwnedByCurrentUser(rental, userDetails.getUsername())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Not authorized"));
            }

            long totalAmount = rental.getTotalRentFee() + rental.getDepositFee();

            // Create MoMo payment URL
            String payUrl = momoService.createPaymentUrl(rentalId, totalAmount, orderInfo);

            Map<String, String> response = new HashMap<>();
            response.put("payUrl", payUrl);
            response.put("rentalId", rentalId);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create payment: " + e.getMessage()));
        }
    }

    /**
     * Handle MoMo IPN callback (server-to-server)
     * POST /api/payment/momo/ipn
     *
     * This endpoint receives asynchronous notifications from MoMo
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<Map<String, String>> handleMoMoIPN(@RequestBody Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validate signature
            if (!momoService.validateIPNCallback(params)) {
                System.err.println("Invalid MoMo IPN signature");
                response.put("errorCode", "99");
                response.put("message", "Invalid signature");
                return ResponseEntity.badRequest().body(response);
            }

            String orderId = params.get("orderId");
            String transId = params.get("transId");
            String errorCode = params.get("errorCode");
            String amount = params.get("amount");
            String message = params.get("message");

            if (orderId == null || orderId.isBlank() || transId == null || transId.isBlank()) {
                response.put("errorCode", "99");
                response.put("message", "Missing required IPN fields");
                return ResponseEntity.badRequest().body(response);
            }

            // MoMo may retry callbacks; return success when transaction already recorded.
            if (paymentTransactionRepository.findByTransactionRef(transId).isPresent()) {
                response.put("errorCode", "0");
                response.put("message", "success");
                return ResponseEntity.ok(response);
            }

            // Parse amount
            long paymentAmount = 0;
            if (amount != null && !amount.isEmpty()) {
                paymentAmount = Long.parseLong(amount);
            }

            // Check payment result
            boolean isSuccess = "0".equals(errorCode);

            // Save payment transaction
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .transactionRef(transId)
                    .orderCode(orderId)
                    .amount((double) paymentAmount)
                    .paymentMethod("MoMo")
                    .responseCode(errorCode)
                    .responseMessage(message)
                    .status(isSuccess ? PaymentTransaction.PaymentStatus.SUCCESS
                            : PaymentTransaction.PaymentStatus.FAILED)
                    .build();

            // Check if this is for an order or rental
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();

                long expectedAmount = order.getTotalAmount()
                        + (order.getShippingFee() != null ? order.getShippingFee() : 0L);
                if (paymentAmount != expectedAmount) {
                    response.put("errorCode", "99");
                    response.put("message", "Amount mismatch");
                    return ResponseEntity.badRequest().body(response);
                }

                transaction.setOrder(order);
                paymentTransactionRepository.save(transaction);

                if (isSuccess) {
                    // Payment successful
                    order.setPaymentStatus("SUCCESS");
                    if (order.getStatus() == Order.OrderStatus.PENDING) {
                        order.setStatus(Order.OrderStatus.SHIPPED);
                    }
                    orderRepository.save(order);

                    // Send confirmation email
                    try {
                        sendOrderConfirmationEmail(order);
                    } catch (MessagingException e) {
                        System.err.println("Failed to send confirmation email: " + e.getMessage());
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

                    long expectedAmount = rental.getTotalRentFee() + rental.getDepositFee();
                    if (paymentAmount != expectedAmount) {
                        response.put("errorCode", "99");
                        response.put("message", "Amount mismatch");
                        return ResponseEntity.badRequest().body(response);
                    }

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

            // Respond to MoMo with success
            response.put("errorCode", "0");
            response.put("message", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error handling MoMo IPN: " + e.getMessage());
            response.put("errorCode", "99");
            response.put("message", "Internal error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Handle MoMo redirect callback (user redirected after payment)
     * GET /api/payment/momo/callback
     *
     * This endpoint handles user redirect after payment completion
     */
    @GetMapping("/momo/callback")
    public ResponseEntity<Void> handleMoMoRedirect(@RequestParam Map<String, String> params) {
        try {
            // Validate signature
            if (!momoService.validateRedirectCallback(params)) {
                System.err.println("Invalid MoMo redirect signature");
                return ResponseEntity.status(302)
                        .header("Location", frontendUrl + "/payment-failed?error=invalid_signature")
                        .build();
            }

            String orderId = params.get("orderId");
            String errorCode = params.get("errorCode");
            boolean isSuccess = "0".equals(errorCode);

            // Redirect to frontend success/failure page
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
     * Get payment status for an order
     * GET /api/payment/status/{orderCode}
     */
    @GetMapping("/status/{orderCode}")
    public ResponseEntity<ApiResponse> getPaymentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderCode) {
        try {
            Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository
                    .findTopByOrderCodeOrderByCreatedAtDesc(orderCode);

            if (transactionOpt.isPresent()) {
                PaymentTransaction transaction = transactionOpt.get();

                if (transaction.getOrder() != null
                        && !isOrderOwnedByCurrentUser(transaction.getOrder(), userDetails.getUsername())) {
                    return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
                }

                if (transaction.getRental() != null
                        && !isRentalOwnedByCurrentUser(transaction.getRental(), userDetails.getUsername())) {
                    return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", transaction.getStatus() == PaymentTransaction.PaymentStatus.SUCCESS);
                result.put("message", transaction.getResponseMessage());
                result.put("orderCode", orderCode);
                result.put("amount", transaction.getAmount());
                result.put("transactionRef", transaction.getTransactionRef());
                result.put("paymentMethod", transaction.getPaymentMethod());
                return ResponseEntity.ok(ApiResponse.success(result));
            }

            // Check order payment status
            Optional<Order> orderOpt = orderRepository.findById(orderCode);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();

                if (!isOrderOwnedByCurrentUser(order, userDetails.getUsername())) {
                    return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", "SUCCESS".equals(order.getPaymentStatus()));
                result.put("orderCode", orderCode);
                result.put("amount", (double) order.getTotalAmount());
                result.put("paymentStatus", order.getPaymentStatus());
                return ResponseEntity.ok(ApiResponse.success(result));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Payment not found");
            result.put("orderCode", orderCode);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get payment status: " + e.getMessage()));
        }
    }

    /**
     * Query MoMo transaction status
     * POST /api/payment/momo/query
     */
    @PostMapping("/momo/query")
    public ResponseEntity<ApiResponse> queryMoMoTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        try {
            String orderId = body.get("orderId");
            String requestId = body.get("requestId");

            if (orderId == null || requestId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("orderId and requestId are required"));
            }

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent() && !isOrderOwnedByCurrentUser(orderOpt.get(), userDetails.getUsername())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }

            Optional<Rental> rentalOpt = rentalRepository.findById(orderId);
            if (rentalOpt.isPresent() && !isRentalOwnedByCurrentUser(rentalOpt.get(), userDetails.getUsername())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }

            Map<String, Object> result = momoService.queryTransaction(orderId, requestId);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to query transaction: " + e.getMessage()));
        }
    }

    private boolean isOrderOwnedByCurrentUser(Order order, String userEmail) {
        return order.getUser() != null && order.getUser().getEmail() != null
                && order.getUser().getEmail().equals(userEmail);
    }

    private boolean isRentalOwnedByCurrentUser(Rental rental, String userEmail) {
        return rental.getUser() != null && rental.getUser().getEmail() != null
                && rental.getUser().getEmail().equals(userEmail);
    }

    private void sendOrderConfirmationEmail(Order order) throws MessagingException {
        // Get user email from order
        String userEmail = order.getUser().getEmail();
        String userName = order.getUser().getUserName();

        // Build order details string
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
