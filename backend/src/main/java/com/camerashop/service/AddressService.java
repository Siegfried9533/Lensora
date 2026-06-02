package com.camerashop.service;

import com.camerashop.dto.AddressDTO;
import com.camerashop.entity.User;
import com.camerashop.entity.UserAddress;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.UserAddressRepository;
import com.camerashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Address book: list / add / update / delete shipping addresses and manage which one is the
 * default. Exactly one address per user is the default at any time.
 */
@Service
public class AddressService {

    @Autowired
    private UserAddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public List<AddressDTO> getAddresses(String email) {
        User user = requireUser(email);
        return addressRepository.findByUserId(user.getUserId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO addAddress(String email, Map<String, Object> body) {
        User user = requireUser(email);

        UserAddress address = UserAddress.builder()
                .user(user)
                .recipientName(requireText(body, "recipientName", "Vui lòng nhập tên người nhận"))
                .recipientPhone(requireText(body, "recipientPhone", "Vui lòng nhập số điện thoại người nhận"))
                .provinceId(str(body, "provinceId"))
                .provinceName(str(body, "provinceName"))
                .districtId(str(body, "districtId"))
                .districtName(str(body, "districtName"))
                .wardCode(str(body, "wardCode"))
                .wardName(str(body, "wardName"))
                .street(str(body, "street"))
                .note(str(body, "note"))
                .postalCode(str(body, "postalCode"))
                .build();

        List<UserAddress> existing = addressRepository.findByUserId(user.getUserId());
        boolean isFirst = existing.isEmpty();
        boolean makeDefault = bool(body, "isDefault") || isFirst;
        if (makeDefault) {
            clearDefault(existing);
        }
        address.setDefault(makeDefault);

        addressRepository.save(address);
        return toDTO(address);
    }

    @Transactional
    public AddressDTO updateAddress(String email, String addressId, Map<String, Object> body) {
        User user = requireUser(email);
        UserAddress address = requireOwned(user, addressId);

        address.setRecipientName(requireText(body, "recipientName", "Vui lòng nhập tên người nhận"));
        address.setRecipientPhone(requireText(body, "recipientPhone", "Vui lòng nhập số điện thoại người nhận"));
        address.setProvinceId(str(body, "provinceId"));
        address.setProvinceName(str(body, "provinceName"));
        address.setDistrictId(str(body, "districtId"));
        address.setDistrictName(str(body, "districtName"));
        address.setWardCode(str(body, "wardCode"));
        address.setWardName(str(body, "wardName"));
        address.setStreet(str(body, "street"));
        address.setNote(str(body, "note"));
        address.setPostalCode(str(body, "postalCode"));

        if (bool(body, "isDefault") && !address.isDefault()) {
            clearDefault(addressRepository.findByUserId(user.getUserId()));
            address.setDefault(true);
        }

        addressRepository.save(address);
        return toDTO(address);
    }

    @Transactional
    public void deleteAddress(String email, String addressId) {
        User user = requireUser(email);
        UserAddress address = requireOwned(user, addressId);
        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            List<UserAddress> remaining = addressRepository.findByUserId(user.getUserId());
            if (!remaining.isEmpty()) {
                UserAddress next = remaining.get(0);
                next.setDefault(true);
                addressRepository.save(next);
            }
        }
    }

    @Transactional
    public AddressDTO setDefault(String email, String addressId) {
        User user = requireUser(email);
        List<UserAddress> all = addressRepository.findByUserId(user.getUserId());
        UserAddress target = null;
        for (UserAddress a : all) {
            boolean match = a.getAddressId().equals(addressId);
            a.setDefault(match);
            if (match) {
                target = a;
            }
        }
        if (target == null) {
            throw new ResourceNotFoundException("Không tìm thấy địa chỉ");
        }
        addressRepository.saveAll(all);
        return toDTO(target);
    }

    private void clearDefault(List<UserAddress> addresses) {
        for (UserAddress a : addresses) {
            if (a.isDefault()) {
                a.setDefault(false);
            }
        }
        addressRepository.saveAll(addresses);
    }

    private UserAddress requireOwned(User user, String addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Không có quyền truy cập địa chỉ này");
        }
        return address;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private String requireText(Map<String, Object> body, String key, String message) {
        String value = str(body, key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value;
    }

    private String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private boolean bool(Map<String, Object> body, String key) {
        return Boolean.parseBoolean(String.valueOf(body.getOrDefault(key, false)));
    }

    private AddressDTO toDTO(UserAddress a) {
        return AddressDTO.builder()
                .addressId(a.getAddressId())
                .recipientName(a.getRecipientName())
                .recipientPhone(a.getRecipientPhone())
                .provinceId(a.getProvinceId())
                .provinceName(a.getProvinceName())
                .districtId(a.getDistrictId())
                .districtName(a.getDistrictName())
                .wardCode(a.getWardCode())
                .wardName(a.getWardName())
                .street(a.getStreet())
                .note(a.getNote())
                .postalCode(a.getPostalCode())
                .isDefault(a.isDefault())
                .build();
    }
}
