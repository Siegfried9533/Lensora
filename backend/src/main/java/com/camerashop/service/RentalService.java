package com.camerashop.service;

import com.camerashop.dto.RentalDTO;
import com.camerashop.entity.*;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetImageRepository assetImageRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Kiem tra thiet bi co san trong khoang ngay cho truoc khong
     */
    public boolean isAssetAvailable(String assetId, LocalDate startDate, LocalDate endDate) {
        // Kiem tra thiet bi ton tai va co san
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null || asset.getStatus() != Asset.AssetStatus.AVAILABLE) {
            return false;
        }

        // Kiem tra cac don thue chong cheo
        List<Rental> existingRentals = rentalRepository.findByAssetId(assetId);
        for (Rental rental : existingRentals) {
            // Bo qua cac don thue da huy hoac hoan thanh
            if (rental.getStatus() == Rental.RentalStatus.CANCELLED ||
                rental.getStatus() == Rental.RentalStatus.COMPLETED) {
                continue;
            }

            // Kiem tra chong cheo ngay
            boolean overlaps = !(endDate.isBefore(rental.getStartDate()) ||
                                startDate.isAfter(rental.getEndDate()));
            if (overlaps) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public RentalDTO createRental(String email, String assetId, LocalDate startDate, LocalDate endDate,
                                   String shippingAddress, String paymentMethod, Long shippingFee) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị cho thuê"));

        if (asset.getStatus() != Asset.AssetStatus.AVAILABLE) {
            throw new RuntimeException("Thiết bị không có sẵn để thuê");
        }

        // Kiem tra tinh san co trong khoang ngay
        if (!isAssetAvailable(assetId, startDate, endDate)) {
            throw new RuntimeException("Thiết bị không có sẵn trong khoảng thời gian đã chọn");
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Tinh phi
        long totalRentFee = asset.getDailyRate() * days;
        long depositFee = asset.getDailyRate() * 3; // Dat coc 3 ngay

        Rental.PaymentMethod paymentMethodEnum = Rental.PaymentMethod.valueOf(paymentMethod);

        Rental rental = Rental.builder()
                .user(user)
                .asset(asset)
                .startDate(startDate)
                .endDate(endDate)
                .depositFee(depositFee)
                .totalRentFee(totalRentFee)
                .penaltyFee(0L)
                .status(Rental.RentalStatus.PENDING)
                .shippingAddress(shippingAddress)
                .paymentMethod(paymentMethodEnum)
                .shippingFee(shippingFee)
                .build();

        rentalRepository.save(rental);

        return toDTO(rental);
    }

    /**
     * Gia han thoi gian thue
     */
    @Transactional
    public RentalDTO extendRental(String rentalId, LocalDate newEndDate) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuê"));

        if (rental.getStatus() != Rental.RentalStatus.ACTIVE &&
            rental.getStatus() != Rental.RentalStatus.PENDING) {
            throw new RuntimeException("Không thể gia hạn đơn thuê với trạng thái: " + rental.getStatus());
        }

        if (newEndDate.isBefore(rental.getEndDate())) {
            throw new RuntimeException("Ngày kết thúc mới phải sau ngày kết thúc hiện tại");
        }

        long additionalDays = java.time.temporal.ChronoUnit.DAYS.between(rental.getEndDate(), newEndDate);
        long additionalFee = rental.getAsset().getDailyRate() * additionalDays;

        rental.setEndDate(newEndDate);
        rental.setTotalRentFee(rental.getTotalRentFee() + additionalFee);

        rentalRepository.save(rental);

        return toDTO(rental);
    }

    /**
     * Xu ly tra thiet bi
     */
    @Transactional
    public RentalDTO returnRental(String rentalId, LocalDate returnDate) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuê"));

        if (rental.getStatus() == Rental.RentalStatus.COMPLETED) {
            throw new RuntimeException("Đơn thuê đã hoàn thành");
        }

        rental.setReturnDate(returnDate);

        // Tinh phi phat neu tra muon
        if (returnDate.isAfter(rental.getEndDate())) {
            long lateDays = java.time.temporal.ChronoUnit.DAYS.between(rental.getEndDate(), returnDate);
            long penaltyRate = rental.getAsset().getDailyRate() * 2; // Gap 2 lan gia ngay cho phi phat
            rental.setPenaltyFee(penaltyRate * lateDays);

            // Gui thong bao qua han
            try {
                notificationService.notifyRentalOverdue(rental, lateDays);
            } catch (Exception e) {
                System.err.println("Failed to send overdue notification: " + e.getMessage());
            }
        }

        rental.setStatus(Rental.RentalStatus.COMPLETED);

        // Cap nhat trang thai thiet bi tro lai co san
        Asset asset = rental.getAsset();
        asset.setStatus(Asset.AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        rentalRepository.save(rental);

        return toDTO(rental);
    }

    /**
     * Tinh gia thue ma khong tao don thue
     */
    public Map<String, Object> calculateRentalPrice(String assetId, LocalDate startDate, LocalDate endDate) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị cho thuê"));

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        long totalRentFee = asset.getDailyRate() * days;
        long depositFee = asset.getDailyRate() * 3;

        return Map.of(
                "dailyRate", asset.getDailyRate(),
                "days", days,
                "totalRentFee", totalRentFee,
                "depositFee", depositFee,
                "total", totalRentFee + depositFee
        );
    }

    public List<RentalDTO> getRentalsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return rentalRepository.findByUserId(user.getUserId(), org.springframework.data.domain.PageRequest.of(0, 100))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RentalDTO getRentalById(String rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuê"));
        return toDTO(rental);
    }

    private RentalDTO toDTO(Rental rental) {
        String primaryImageUrl = null;
        AssetImage img = assetImageRepository.findByAssetIdAndIsPrimaryTrue(rental.getAsset().getAssetId());
        if (img != null) {
            primaryImageUrl = img.getUrl();
        }

        return RentalDTO.builder()
                .rentalId(rental.getRentalId())
                .userId(rental.getUser().getUserId())
                .assetId(rental.getAsset().getAssetId())
                .assetName(rental.getAsset().getModelName())
                .assetBrand(rental.getAsset().getBrand())
                .primaryImageUrl(primaryImageUrl)
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .returnDate(rental.getReturnDate())
                .depositFee(rental.getDepositFee())
                .totalRentFee(rental.getTotalRentFee())
                .penaltyFee(rental.getPenaltyFee())
                .status(rental.getStatus().name())
                .shippingAddress(rental.getShippingAddress())
                .paymentMethod(rental.getPaymentMethod() != null ? rental.getPaymentMethod().name() : null)
                .shippingFee(rental.getShippingFee())
                .build();
    }
}
