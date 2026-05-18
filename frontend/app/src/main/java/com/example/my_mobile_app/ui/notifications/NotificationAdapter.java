package com.example.my_mobile_app.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.model.Notification;
import com.example.my_mobile_app.util.DateUtils;

import java.util.Date;
import java.util.List;

/** Notification list adapter with unread dot and type chip. */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnClick {
        void onNotificationClick(Notification notification);
    }

    private final List<Notification> items;
    private final OnClick onClick;

    public NotificationAdapter(List<Notification> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = items.get(position);
        h.txtIcon.setText(iconFor(n.type));
        h.txtTitle.setText(valueOrDash(n.title));
        h.txtMessage.setText(valueOrDash(n.message));
        h.txtType.setText(labelFor(n.type));
        Date date = DateUtils.parseIso(n.createdAt);
        String created = DateUtils.formatDisplay(date);
        h.txtTime.setText(created.isEmpty() ? "--" : created);
        h.dotUnread.setVisibility(n.isRead ? View.GONE : View.VISIBLE);
        h.itemView.setAlpha(n.isRead ? 0.72f : 1f);
        h.itemView.setOnClickListener(v -> onClick.onNotificationClick(n));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String iconFor(String type) {
        String t = type == null ? "" : type;
        if (t.contains("PAYMENT")) return "₫";
        if (t.contains("ORDER")) return "#";
        if (t.contains("RENTAL")) return "R";
        if (t.contains("SHIPPING")) return "→";
        if (t.contains("PROMOTION")) return "↗";
        return "!";
    }

    private static String labelFor(String type) {
        String t = type == null ? "" : type;
        switch (t) {
            case "ORDER_UPDATE": return "Đơn hàng";
            case "PAYMENT_SUCCESS": return "Thanh toán";
            case "PAYMENT_FAILED": return "Thanh toán";
            case "RENTAL_REMINDER": return "Thuê";
            case "RENTAL_OVERDUE": return "Quá hạn";
            case "SHIPPING_UPDATE": return "Giao hàng";
            case "PROMOTION": return "Ưu đãi";
            case "SYSTEM": return "Hệ thống";
            default: return t.isEmpty() ? "Thông báo" : t;
        }
    }

    private static String valueOrDash(String value) {
        return value == null || value.isEmpty() ? "--" : value;
    }

    static class VH extends RecyclerView.ViewHolder {
        final View dotUnread;
        final TextView txtIcon, txtTitle, txtMessage, txtTime, txtType;

        VH(@NonNull View itemView) {
            super(itemView);
            dotUnread = itemView.findViewById(R.id.dot_unread);
            txtIcon = itemView.findViewById(R.id.txt_icon);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtMessage = itemView.findViewById(R.id.txt_message);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtType = itemView.findViewById(R.id.txt_type);
        }
    }
}
