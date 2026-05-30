package com.camerashop.service;

import com.camerashop.dto.PaymentMethodDTO;
import com.camerashop.entity.SavedPaymentMethod;
import com.camerashop.entity.User;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.SavedPaymentMethodRepository;
import com.camerashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentMethodService {

    @Autowired
    private SavedPaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    public List<PaymentMethodDTO> getMethods(String email) {
        User user = requireUser(email);
        return paymentMethodRepository.findByUserId(user.getUserId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Add a saved method. The raw {@code account} (card/phone) is masked here and only the
     * masked form is persisted — no full account numbers or credentials are ever stored.
     */
    @Transactional
    public PaymentMethodDTO addMethod(String email, String typeStr, String label,
                                      String accountHolder, String account, boolean makeDefault) {
        User user = requireUser(email);

        SavedPaymentMethod.MethodType type;
        try {
            type = SavedPaymentMethod.MethodType.valueOf(typeStr == null ? "" : typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Loại phương thức thanh toán không hợp lệ. Chọn MOMO hoặc BANK.");
        }
        if (label == null || label.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập tên phương thức (ví dụ: Vietcombank, Ví MoMo)");
        }

        List<SavedPaymentMethod> existing = paymentMethodRepository.findByUserId(user.getUserId());
        boolean isFirst = existing.isEmpty();

        if (makeDefault || isFirst) {
            // Only one default per user.
            for (SavedPaymentMethod m : existing) {
                if (m.isDefault()) {
                    m.setDefault(false);
                }
            }
            paymentMethodRepository.saveAll(existing);
        }

        SavedPaymentMethod method = SavedPaymentMethod.builder()
                .user(user)
                .type(type)
                .label(label.trim())
                .accountHolder(accountHolder == null ? null : accountHolder.trim())
                .maskedAccount(maskAccount(account))
                .isDefault(makeDefault || isFirst)
                .build();

        paymentMethodRepository.save(method);
        return toDTO(method);
    }

    @Transactional
    public void deleteMethod(String email, String methodId) {
        User user = requireUser(email);
        SavedPaymentMethod method = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương thức thanh toán"));
        if (!method.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Không có quyền truy cập");
        }
        boolean wasDefault = method.isDefault();
        paymentMethodRepository.delete(method);

        // If we removed the default, promote the most recent remaining method.
        if (wasDefault) {
            List<SavedPaymentMethod> remaining = paymentMethodRepository.findByUserId(user.getUserId());
            if (!remaining.isEmpty()) {
                SavedPaymentMethod next = remaining.get(0);
                next.setDefault(true);
                paymentMethodRepository.save(next);
            }
        }
    }

    @Transactional
    public PaymentMethodDTO setDefault(String email, String methodId) {
        User user = requireUser(email);
        List<SavedPaymentMethod> all = paymentMethodRepository.findByUserId(user.getUserId());
        SavedPaymentMethod target = null;
        for (SavedPaymentMethod m : all) {
            boolean match = m.getPaymentMethodId().equals(methodId);
            m.setDefault(match);
            if (match) {
                target = m;
            }
        }
        if (target == null) {
            throw new ResourceNotFoundException("Không tìm thấy phương thức thanh toán");
        }
        paymentMethodRepository.saveAll(all);
        return toDTO(target);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    /**
     * Keep only the last 4 visible. Works for both card/account numbers and phone numbers.
     * Returns null for blank input.
     */
    private String maskAccount(String account) {
        if (account == null) {
            return null;
        }
        String digits = account.replaceAll("\\s+", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() <= 4) {
            return "**** " + digits;
        }
        String last4 = digits.substring(digits.length() - 4);
        return "**** " + last4;
    }

    private PaymentMethodDTO toDTO(SavedPaymentMethod m) {
        return PaymentMethodDTO.builder()
                .paymentMethodId(m.getPaymentMethodId())
                .type(m.getType().name())
                .label(m.getLabel())
                .accountHolder(m.getAccountHolder())
                .maskedAccount(m.getMaskedAccount())
                .isDefault(m.isDefault())
                .build();
    }
}
