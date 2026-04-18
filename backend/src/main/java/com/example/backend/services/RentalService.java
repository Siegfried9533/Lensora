package com.example.backend.services;

import com.example.backend.dto.RentalRequest;
import com.example.backend.dto.RentalResponse;
import com.example.backend.entity.Asset;
import com.example.backend.entity.Image;
import com.example.backend.entity.Rental;
import com.example.backend.entity.User;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.ImageRepository;
import com.example.backend.repository.RentalRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RentalService {
        private final RentalRepository rentalRepository;
        private final UserRepository userRepository;
        private final AssetRepository assetRepository;
        private final ImageRepository imageRepository;

        // Tạo đơn thuê mới
        @Transactional
        public RentalResponse createRental(RentalRequest request, String userEmail) {
                // Lấy user theo email trong JWT để tránh giả mạo userId ở request body.
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Kiểm tra asset hợp lệ
                Asset asset = assetRepository.findById(request.getAssetId())
                                .orElseThrow(() -> new RuntimeException("Asset not found"));

                // kiểm tra trùng lịch
                List<Rental> conflictingRentals = rentalRepository.findConflictingRentals(
                                request.getAssetId(),
                                request.getStartDate(),
                                request.getEndDate());

                if (!conflictingRentals.isEmpty()) {
                        throw new RuntimeException("Asset is already rented for the selected dates");
                }

                // tính chi phí
                Double depositFee = asset.getDepositValue();
                long rentalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
                Double totalRentFee = rentalDays * asset.getDailyRate();
                Double penaltyFee = 0.0; // phí phạt sẽ tính khi trả muộn

                // tạo đơn thuê
                Rental rental = new Rental();
                rental.setUser(user);
                rental.setAsset(asset);
                rental.setStartDate(request.getStartDate());
                rental.setEndDate(request.getEndDate());
                rental.setDepositFee(depositFee);
                rental.setTotalRentFee(totalRentFee);
                rental.setPenaltyFee(penaltyFee);
                rental.setStatus("PENDING");
                Rental savedRental = rentalRepository.save(rental);

                return mapToResponse(savedRental);
        }

        // Trả tài sản
        @Transactional
        public RentalResponse returnAsset(Long rentalId, String userEmail) {
                Rental rental = rentalRepository.findById(rentalId)
                                .orElseThrow(() -> new RuntimeException("Rental not found"));

                if (!rental.getUser().getEmail().equals(userEmail)) {
                        throw new RuntimeException("You can only return your own rentals");
                }

                if (!"ACTIVE".equals(rental.getStatus())) {
                        throw new RuntimeException("This rental is already completed or cancelled");
                }

                // cập nhật ngày trả và trạng thái
                LocalDate now = LocalDate.now();
                rental.setReturnDate(now);
                rental.setStatus("COMPLETED");

                // cập nhật trạng thái Asset về AVAILABLE
                Asset asset = rental.getAsset();
                asset.setStatus("AVAILABLE");

                // tính số ngày trễ hạng
                long daysLate = ChronoUnit.DAYS.between(rental.getEndDate(), now);
                if (daysLate > 0) {
                        Double penaltyFee = daysLate * rental.getAsset().getDailyRate() * 1.5; // phí phạt 150% giá thuê
                        rental.setPenaltyFee(penaltyFee);
                        rentalRepository.save(rental);
                }
                // lưu rental với ngày trả và trạng thái mới
                return mapToResponse(rental);
        }

        // Lấy lịch sử thuê của một người dùng
        public List<RentalResponse> getUserRentalHistory(String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return rentalRepository.findByUserUserIdOrderByStartDateDesc(user.getUserId())
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        private RentalResponse mapToResponse(Rental rental) {
                // Lấy ảnh đại diện (Primary) để hiện lên đơn hàng cho đẹp
                String primaryImg = imageRepository.findByEntityIdAndType(rental.getAsset().getAssetId(), "ASSET")
                                .stream()
                                .filter(Image::getIsPrimary)
                                .map(Image::getUrl)
                                .findFirst()
                                .orElse(null);

                return RentalResponse.builder()
                                .rentalId(rental.getRentalId())
                                .userName(rental.getUser().getUserName())
                                .email(rental.getUser().getEmail())
                                .assetId(rental.getAsset().getAssetId())
                                .modelName(rental.getAsset().getModelName())
                                .brand(rental.getAsset().getBrand())
                                .primaryImageUrl(primaryImg)
                                .startDate(rental.getStartDate())
                                .endDate(rental.getEndDate())
                                .depositFee(rental.getDepositFee())
                                .totalRentFee(rental.getTotalRentFee())
                                .status(rental.getStatus())
                                .build();
        }
}
