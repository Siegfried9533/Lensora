package com.example.my_mobile_app.util;

import com.example.my_mobile_app.R;

/** Maps order/rental status strings to label, color, and chip background. */
public final class StatusUtils {

    public static final class Style {
        public final String label;
        public final int colorRes;
        public final int chipBgRes;
        Style(String label, int colorRes, int chipBgRes) {
            this.label = label;
            this.colorRes = colorRes;
            this.chipBgRes = chipBgRes;
        }
    }

    private StatusUtils() {}

    public static Style forOrder(String status) {
        String s = status == null ? "" : status.toUpperCase();
        switch (s) {
            case "DELIVERED":
                return new Style("Đã giao", R.color.green_500, R.drawable.bg_status_delivered);
            case "SHIPPING":
            case "SHIPPED":
                return new Style("Đang vận chuyển", R.color.blue_500, R.drawable.bg_status_shipping);
            case "CONFIRMED":
                return new Style("Đã xác nhận", R.color.blue_500, R.drawable.bg_status_shipping);
            case "PROCESSING":
                return new Style("Đang xử lý", R.color.blue_500, R.drawable.bg_status_shipping);
            case "CANCELLED":
                return new Style("Đã hủy", R.color.red_500, R.drawable.bg_status_cancelled);
            case "PENDING":
                return new Style("Chờ xử lý", R.color.yellow_500, R.drawable.bg_status_pending);
            default:
                return new Style(status == null ? "" : status,
                        R.color.text_muted, R.drawable.bg_status_pending);
        }
    }

    public static Style forRental(String status) {
        String s = status == null ? "" : status.toUpperCase();
        switch (s) {
            case "ACTIVE":
                return new Style("Đang hoạt động", R.color.blue_500, R.drawable.bg_status_shipping);
            case "COMPLETED":
                return new Style("Hoàn thành", R.color.green_500, R.drawable.bg_status_delivered);
            case "CANCELLED":
                return new Style("Đã hủy", R.color.red_500, R.drawable.bg_status_cancelled);
            case "PENDING":
                return new Style("Chờ xử lý", R.color.yellow_500, R.drawable.bg_status_pending);
            default:
                return new Style(status == null ? "" : status,
                        R.color.text_muted, R.drawable.bg_status_pending);
        }
    }
}
