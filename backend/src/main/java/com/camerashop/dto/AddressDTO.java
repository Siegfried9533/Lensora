package com.camerashop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * A saved shipping address, as exposed to the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private String addressId;
    private String recipientName;
    private String recipientPhone;
    private String provinceId;
    private String provinceName;
    private String districtId;
    private String districtName;
    private String wardCode;
    private String wardName;
    private String street;
    private String note;
    private String postalCode;
    @JsonProperty("isDefault")
    private boolean isDefault;
}
