package com.camerashop.service;

import com.camerashop.dto.RentalDTO;
import com.camerashop.entity.*;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return !hasOverlappingRental(assetId, startDate, endDate);
    }

    /**
     * True if an active/pending rental already covers any day in [startDate, endDate].
     * Cancelled and completed rentals do not block the slot.
     */
    private boolean hasOverlappingRental(String assetId, LocalDate startDate, LocalDate endDate) {
        List<Rental> existingRentals = rentalRepository.findByAssetId(assetId);
        for (Rental rental : existingRentals) {
            if (rental.getStatus() == Rental.RentalStatus.CANCELLED ||
                rental.getStatus() == Rental.RentalStatus.COMPLETED) {
                continue;
            }
            boolean overlaps = !(endDate.isBefore(rental.getStartDate()) ||
                                startDate.isAfter(rental.getEndDate()));
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public RentalDTO createRental(String email, String assetId, LocalDate startDate, LocalDate endDate,
                                   String shippingAddress, String paymentMethod, Long shippingFee) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // Pay-first: rentals cannot be COD. Only online methods (MoMo) are accepted; the
        // asset is handed over only after payment succeeds (IPN flips status to ACTIVE).
        Rental.PaymentMethod paymentMethodEnum;
        try {
            paymentMethodEnum = Rental.PaymentMethod.valueOf(
                    paymentMethod == null ? "" : paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Thuê thiết bị yêu cầu thanh toán trước. Vui lòng chọn phương thức thanh toán trực tuyến (MoMo).");
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Acquire a row-level write lock on the asset so concurrent rental requests for the
        // same asset are serialized. The overlap re-check below runs while holding the lock —
        // this is the authoritative guard against two people renting the same camera at once.
        Asset asset = assetRepository.findByIdForUpdate(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị cho thuê"));

        if (asset.getStatus() != Asset.AssetStatus.AVAILABLE) {
            throw new RuntimeException("Thiết bị không có sẵn để thuê");
        }

        if (hasOverlappingRental(assetId, startDate, endDate)) {
            throw new RuntimeException("Thiết bị đã được thuê trong khoảng thời gian đã chọn");
        }

        // Tinh phi
        long totalRentFee = asset.getDailyRate() * days;
        long depositFee = asset.getDailyRate() * 3; // Dat coc 3 ngay

        Rental rental = Rental.builder()
                .user(user)
                .asset(asset)
                .startDate(startDate)
                .endDate(endDate)
                .depositFee(depositFee)
                .totalRentFee(totalRentFee)
                .penaltyFee(0L)
                .status(Rental.RentalStatus.PENDING)
                .paymentStatus("PENDING")
                .shippingAddress(shippingAddress)
                .paymentMethod(paymentMethodEnum)
                .shippingFee(shippingFee)
                .build();

        rentalRepository.save(rental);

        return toDTO(rental);
    }

    /**
     * Xóa hẳn đơn thuê vừa tạo khi KHỞI TẠO THANH TOÁN THẤT BẠI, để "lỗi thì không tạo gì cả".
     * Chỉ xóa khi đơn còn PENDING và chưa thanh toán thành công — tránh xóa nhầm đơn đã trả tiền.
     * Trả lại trạng thái như chưa từng thuê (không để lại đơn rác trong màn hình giao dịch).
     */
    @Transactional
    public void deleteRentalIfUnpaid(String rentalId) {
        if (rentalId == null) {
            return;
        }
        rentalRepository.findById(rentalId).ifPresent(rental -> {
            boolean unpaid = !"SUCCESS".equals(rental.getPaymentStatus());
            if (rental.getStatus() == Rental.RentalStatus.PENDING && unpaid) {
                rentalRepository.delete(rental);
            }
        });
    }

    /**
     * Release rental holds that were created (PENDING) but never paid within the window.
     * Without this, an abandoned unpaid checkout would block the camera's dates forever.
     * Returns the number of holds released. Invoked by the scheduled job.
     */
    @Transactional
    public int releaseExpiredHolds(int holdMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(holdMinutes);
        List<Rental> expired = rentalRepository.findExpiredHolds(Rental.RentalStatus.PENDING, cutoff);
        for (Rental rental : expired) {
            rental.setStatus(Rental.RentalStatus.CANCELLED);
            rental.setPaymentStatus("EXPIRED");
        }
        if (!expired.isEmpty()) {
            rentalRepository.saveAll(expired);
        }
        return expired.size();
    }

    /**
     * Gia han thoi gian thue
     */
    @Transactional
    public RentalDTO extendRental(String rentalId, LocalDate newEndDate) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị cho thuê"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return rentalRepository.findByUserId(
                        user.getUserId(),
                        PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RentalDTO getRentalById(String rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê"));
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
                .paymentStatus(rental.getPaymentStatus())
                .build();
    }
}
