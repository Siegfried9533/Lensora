package com.example.backend.repository;

import java.util.List;
import com.example.backend.entity.Camera;
import com.example.backend.entity.CameraStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CameraRepository extends JpaRepository<Camera, Long> {
    // Tìm các máy ảnh có trạng thái AVAILABLE
    List<Camera> findByStatus(CameraStatus status);
}
