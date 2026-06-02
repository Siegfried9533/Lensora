package com.camerashop.repository;

import com.camerashop.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, String> {

    @Query("SELECT a FROM UserAddress a WHERE a.user.userId = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<UserAddress> findByUserId(@Param("userId") String userId);
}
