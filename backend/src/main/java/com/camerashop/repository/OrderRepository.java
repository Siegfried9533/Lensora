package com.camerashop.repository;

import com.camerashop.entity.Order;
import com.camerashop.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId")
    Page<Order> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") OrderStatus status);

    /**
     * Đơn còn cần đồng bộ trạng thái từ GHN: đã có mã vận đơn và chưa ở trạng thái kết thúc.
     * Dùng bởi job quét định kỳ để phản ánh việc hủy/giao trên GHN về app.
     */
    @Query("SELECT o FROM Order o WHERE o.ghnOrderId IS NOT NULL AND o.ghnOrderId <> '' AND o.status NOT IN :terminal")
    List<Order> findSyncableGhnOrders(@Param("terminal") List<OrderStatus> terminal);
}
