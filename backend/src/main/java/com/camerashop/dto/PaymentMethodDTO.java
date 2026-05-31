package com.camerashop.dto;

import lombok.*;

/**
 * A user's saved payment method, as exposed to the client. Metadata only.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodDTO {
    private String paymentMethodId;
    private String type;          // MOMO, BANK
    private String label;
    private String accountHolder;
    private String maskedAccount;
    private boolean isDefault;
}
