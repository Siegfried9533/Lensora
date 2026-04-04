package com.example.backend.repository;

import com.example.backend.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    // Lưu đơn thuê vào cơ sở dữ liệu

}
