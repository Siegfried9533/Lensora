package com.camerashop.repository;

import com.camerashop.entity.SavedPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPaymentMethodRepository extends JpaRepository<SavedPaymentMethod, String> {

    @Query("SELECT m FROM SavedPaymentMethod m WHERE m.user.userId = :userId ORDER BY m.isDefault DESC, m.createdAt DESC")
    List<SavedPaymentMethod> findByUserId(@Param("userId") String userId);

    @Query("SELECT m FROM SavedPaymentMethod m WHERE m.user.userId = :userId")
    List<SavedPaymentMethod> findAllForDefaultReset(@Param("userId") String userId);
}
