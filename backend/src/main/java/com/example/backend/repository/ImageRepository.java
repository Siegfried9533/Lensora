package com.example.backend.repository;

import com.example.backend.entity.Image;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByEntityIdAndType(Long entityId, String type);
    
}