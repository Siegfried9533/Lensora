package com.example.my_mobile_app.model;

/** Mirrors {@code AddressDTO} on the backend — a saved shipping address. */
public class Address {
    public String addressId;
    public String recipientName;
    public String recipientPhone;
    public String provinceId;
    public String provinceName;
    public String districtId;
    public String districtName;
    public String wardCode;
    public String wardName;
    public String street;
    public String note;
    public String postalCode;
    public boolean isDefault;

    public Address() {}

    /** "street, ward, district, province" — blanks skipped. */
    public String fullAddress() {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, street);
        appendPart(sb, wardName);
        appendPart(sb, districtName);
        appendPart(sb, provinceName);
        return sb.toString();
    }

    private void appendPart(StringBuilder sb, String part) {
        if (part == null || part.trim().isEmpty()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(part.trim());
    }
}
