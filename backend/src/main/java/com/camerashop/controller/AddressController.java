package com.camerashop.controller;

import com.camerashop.dto.AddressDTO;
import com.camerashop.dto.ApiResponse;
import com.camerashop.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Shipping address book. All endpoints require authentication; an address belongs to the
 * calling user.
 */
@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        List<AddressDTO> addresses = addressService.getAddresses(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> add(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody Map<String, Object> body) {
        try {
            AddressDTO address = addressService.addAddress(userDetails.getUsername(), body);
            return ResponseEntity.ok(ApiResponse.success(address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable String id,
                                              @RequestBody Map<String, Object> body) {
        try {
            AddressDTO address = addressService.updateAddress(userDetails.getUsername(), id, body);
            return ResponseEntity.ok(ApiResponse.success(address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable String id) {
        try {
            addressService.deleteAddress(userDetails.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse> setDefault(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable String id) {
        try {
            AddressDTO address = addressService.setDefault(userDetails.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success(address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
