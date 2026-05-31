package com.camerashop.config;

import com.camerashop.entity.Notification;
import com.camerashop.entity.Rental;
import com.camerashop.repository.RentalRepository;
import com.camerashop.service.NotificationService;
import com.camerashop.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled jobs for notifications
 * - Rental return reminders (2 days before due date)
 * - Overdue rental notifications
 * - Releasing abandoned unpaid rental holds (pay-first cleanup)
 */
@Component
public class NotificationScheduler {

    /** Minutes a PENDING (unpaid) rental hold is kept before it is auto-cancelled. */
    private static final int RENTAL_HOLD_MINUTES = 15;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RentalService rentalService;

    /**
     * Release abandoned rental holds every minute. A rental created as PENDING blocks the
     * asset's dates; if the user never completes the pay-first payment, this frees the slot
     * so nobody else is locked out — mirroring how real marketplaces expire unpaid carts.
     */
    @Scheduled(fixedRate = 60_000)
    public void releaseExpiredRentalHolds() {
        try {
            int released = rentalService.releaseExpiredHolds(RENTAL_HOLD_MINUTES);
            if (released > 0) {
                System.out.println("Released " + released + " expired rental hold(s)");
            }
        } catch (Exception e) {
            System.err.println("Error releasing expired rental holds: " + e.getMessage());
        }
    }

    /**
     * Send rental return reminders every day at 9 AM
     * Notifies users whose rentals are due in 2 days
     */
    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9 AM
    public void sendRentalReturnReminders() {
        System.out.println("Running scheduled job: Rental Return Reminders");

        try {
            LocalDate twoDaysFromNow = LocalDate.now().plusDays(2);

            // Get all active rentals
            List<Rental> activeRentals = rentalRepository.findByStatus(Rental.RentalStatus.ACTIVE);

            for (Rental rental : activeRentals) {
                // Check if rental is due in 2 days
                if (rental.getEndDate().isEqual(twoDaysFromNow)) {
                    try {
                        notificationService.notifyRentalReturnReminder(rental, 2);
                        System.out.println("Sent reminder for rental: " + rental.getRentalId());
                    } catch (Exception e) {
                        System.err.println("Failed to send reminder for rental " + rental.getRentalId() + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("Rental reminder job completed");

        } catch (Exception e) {
            System.err.println("Error in rental reminder job: " + e.getMessage());
        }
    }

    /**
     * Check for overdue rentals every day at 10 AM
     * Notifies users with overdue rentals (only once per rental to avoid spam)
     */
    @Scheduled(cron = "0 0 10 * * ?") // Every day at 10 AM
    public void checkOverdueRentals() {
        System.out.println("Running scheduled job: Overdue Rental Check");

        try {
            LocalDate today = LocalDate.now();

            // Get all active rentals
            List<Rental> activeRentals = rentalRepository.findByStatus(Rental.RentalStatus.ACTIVE);

            for (Rental rental : activeRentals) {
                // Check if rental is overdue (past end date and not returned)
                if (rental.getEndDate().isBefore(today) && rental.getReturnDate() == null) {
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(rental.getEndDate(), today);

                    try {
                        // Only send if no overdue notification exists yet for this rental
                        boolean alreadyNotified = notificationService.hasOverdueNotification(rental.getRentalId());
                        if (!alreadyNotified) {
                            notificationService.notifyRentalOverdue(rental, daysOverdue);
                            System.out.println("Sent overdue notification for rental: " + rental.getRentalId());
                        } else {
                            System.out.println("Skipped duplicate overdue notification for rental: " + rental.getRentalId());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to send overdue notification for rental " + rental.getRentalId() + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("Overdue rental check completed");

        } catch (Exception e) {
            System.err.println("Error in overdue rental check: " + e.getMessage());
        }
    }
}
