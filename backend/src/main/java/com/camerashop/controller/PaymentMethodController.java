package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.PaymentMethodDTO;
import com.camerashop.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Saved payment methods (bank / MoMo) that a user manages in settings.
 * All endpoints require authentication; a method belongs to the calling user.
 */
@RestController
@RequestMapping("/api/payment-methods")
@CrossOrigin(origins = "*")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        List<PaymentMethodDTO> methods = paymentMethodService.getMethods(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> add(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody Map<String, Object> body) {
        try {
            String type = body.get("type") == null ? null : String.valueOf(body.get("type"));
            String label = body.get("label") == null ? null : String.valueOf(body.get("label"));
            String accountHolder = body.get("accountHolder") == null ? null : String.valueOf(body.get("accountHolder"));
            String account = body.get("account") == null ? null : String.valueOf(body.get("account"));
            boolean makeDefault = Boolean.parseBoolean(String.valueOf(body.getOrDefault("isDefault", false)));

            PaymentMethodDTO method = paymentMethodService.addMethod(
                    userDetails.getUsername(), type, label, accountHolder, account, makeDefault);
            return ResponseEntity.ok(ApiResponse.success(method));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable String id) {
        try {
            paymentMethodService.deleteMethod(userDetails.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse> setDefault(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable String id) {
        try {
            PaymentMethodDTO method = paymentMethodService.setDefault(userDetails.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success(method));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
