package com.example.my_mobile_app.util;

import android.content.Context;

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

    public static Style forOrder(Context context, String status) {
        String s = status == null ? "" : status.toUpperCase();
        switch (s) {
            case "DELIVERED":
                return new Style(context.getString(R.string.status_delivered),
                        R.color.green_500, R.drawable.bg_status_delivered);
            case "SHIPPING":
            case "SHIPPED":
                return new Style(context.getString(R.string.status_in_transit),
                        R.color.blue_500, R.drawable.bg_status_shipping);
            case "CONFIRMED":
                return new Style(context.getString(R.string.status_confirmed),
                        R.color.blue_500, R.drawable.bg_status_shipping);
            case "PROCESSING":
                return new Style(context.getString(R.string.status_processing),
                        R.color.blue_500, R.drawable.bg_status_shipping);
            case "CANCELLED":
                return new Style(context.getString(R.string.status_cancelled),
                        R.color.red_500, R.drawable.bg_status_cancelled);
            case "PENDING":
                // Đơn vừa đặt thành công đang được cửa hàng xử lý → hiển thị "Đang xử lý".
                return new Style(context.getString(R.string.status_processing),
                        R.color.yellow_500, R.drawable.bg_status_pending);
            default:
                return new Style(status == null ? "" : status,
                        R.color.text_muted, R.drawable.bg_status_pending);
        }
    }

    public static Style forRental(Context context, String status) {
        String s = status == null ? "" : status.toUpperCase();
        switch (s) {
            case "ACTIVE":
                return new Style(context.getString(R.string.status_active),
                        R.color.blue_500, R.drawable.bg_status_shipping);
            case "COMPLETED":
                return new Style(context.getString(R.string.status_completed),
                        R.color.green_500, R.drawable.bg_status_delivered);
            case "CANCELLED":
                return new Style(context.getString(R.string.status_cancelled),
                        R.color.red_500, R.drawable.bg_status_cancelled);
            case "PENDING":
                return new Style(context.getString(R.string.status_pending),
                        R.color.yellow_500, R.drawable.bg_status_pending);
            default:
                return new Style(status == null ? "" : status,
                        R.color.text_muted, R.drawable.bg_status_pending);
        }
    }
}
