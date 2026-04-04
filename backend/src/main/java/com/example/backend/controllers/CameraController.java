package com.example.backend.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import com.example.backend.entity.Camera;
import com.example.backend.repository.CameraRepository;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/cameras")
@CrossOrigin(origins = "*")

public class CameraController {
    @Autowired
    private CameraRepository cameraRepo;

    // Lấy danh sách máy ảnh hiển thị lên màn hình chính
    @GetMapping
    public List<Camera> getAllCameras() {
        return cameraRepo.findAll();
    }
}
