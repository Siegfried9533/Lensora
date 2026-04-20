package com.example.backend.repository;

import com.example.backend.entity.Rental;
import com.example.backend.entity.Rental.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, String> {
    @Query("SELECT r FROM Rental r WHERE r.user.userId = :userId")
    Page<Rental> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT r FROM Rental r WHERE r.user.userId = :userId AND r.status = :status")
    List<Rental> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") RentalStatus status);

    @Query("SELECT r FROM Rental r WHERE r.asset.assetId = :assetId")
    List<Rental> findByAssetId(@Param("assetId") String assetId);

    @Query("SELECT r FROM Rental r WHERE r.asset.assetId = :assetId AND r.status IN :statuses")
    List<Rental> findByAssetIdAndActiveStatus(@Param("assetId") String assetId, @Param("statuses") List<RentalStatus> statuses);

    @Query("SELECT r FROM Rental r WHERE r.status = :status")
    List<Rental> findByStatus(@Param("status") RentalStatus status);
}
