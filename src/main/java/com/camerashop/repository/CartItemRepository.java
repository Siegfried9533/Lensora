package com.camerashop.repository;

import com.camerashop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    @Query("SELECT c FROM CartItem c WHERE c.user.userId = :userId")
    List<CartItem> findByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
