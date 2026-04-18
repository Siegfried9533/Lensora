package com.example.backend.repository;

import com.example.backend.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    // Kiểm tra trùng lịch
    @Query("SELECT r FROM Rental r WHERE r.asset.assetId = :assetId AND " +
            "((:startDate BETWEEN r.startDate AND r.endDate) OR " +
            "(:endDate BETWEEN r.startDate AND r.endDate) OR " +
            "(r.startDate BETWEEN :startDate AND :endDate) OR " +
            "(r.endDate BETWEEN :startDate AND :endDate)) AND " +
            "r.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Rental> findConflictingRentals(@Param("assetId") Long assetId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // lấy lịch sử thuê của một người dùng
    List<Rental> findByUserUserIdOrderByStartDateDesc(@Param("userId") Long userId);
}
