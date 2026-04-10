package com.example.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.backend.entity.Rental;
import com.example.backend.entity.Camera;
import jakarta.transaction.Transactional;
import com.example.backend.entity.CameraStatus;
import com.example.backend.entity.RentalStatus;
import com.example.backend.entity.Customer;
import com.example.backend.repository.CameraRepository;
import com.example.backend.repository.CustomerRepository;
import com.example.backend.repository.RentalRepository;
import java.time.LocalDate;

@Service
public class RentalService {
    @Autowired
    private CameraRepository cameraRepo;
    @Autowired
    private CustomerRepository customerRepo;
    @Autowired
    private RentalRepository rentalRepo;

    @Transactional
    // Tạo đơn thuê mới
    public Rental createRental(Long customerId, Long cameraId, int days) {
        // Kiểm tra Camera có tồn tại không
        if (cameraId == null) {
            throw new RuntimeException("CAMERA_ID_REQUIRED");
        }
        Camera camera = cameraRepo.findById(cameraId).orElseThrow(() -> new RuntimeException("CAMERA_NOT_FOUND"));

        // Kiểm tra Camera có sẵn sàng không
        if (camera.getStatus() != CameraStatus.AVAILABLE) {
            throw new RuntimeException("CAMERA_NOT_AVAILABLE");
        }

        if (customerId == null) {
            throw new RuntimeException("CUSTOMER_ID_REQUIRED");
        }

        // Kiểm tra Khách hàng có tồn tại không (Không tự động tạo)
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("CUSTOMER_NOT_FOUND"));

        // Nếu vượt qua hết các kiểm tra trên, mới tiến hành tạo đơn thuê

        camera.setStatus(CameraStatus.RENTING);
        cameraRepo.save(camera);

        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setCamera(camera);
        rental.setStartDate(LocalDate.now());
        rental.setEndDate(LocalDate.now().plusDays(days));
        rental.setStatus(RentalStatus.ACTIVE);
        rental.setTotalRentAmount(camera.getDailyRate() * days);

        return rentalRepo.save(rental);
    }

    // Trả máy
    @Transactional
    public Rental returnCamera(Long rentalId) {
        if (rentalId == null) {
            throw new RuntimeException("RENTAL_ID_REQUIRED");
        }
        Rental rental = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("RENTAL_NOT_FOUND"));

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new RuntimeException("RENTAL_ALREADY_CLOSED");
        }

        // Cập nhật trạng thái máy ảnh
        Camera camera = rental.getCamera();
        camera.setStatus(CameraStatus.AVAILABLE);
        cameraRepo.save(camera);

        // Cập nhật trạng thái đơn thuê
        rental.setStatus(RentalStatus.COMPLETED);
        rental.setReturnDate(LocalDate.now());

        return rentalRepo.save(rental);
    }

}