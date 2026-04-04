package com.example.backend.repository;

import com.example.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Phương thức quan trọng nhất để kiểm tra khách hàng đã tồn tại chưa
    Optional<Customer> findByPhone(String phone);

    // Tìm theo Email nếu cần thiết cho logic đăng nhập
    Optional<Customer> findByEmail(String email);

    boolean existsByPhone(String phone);
}