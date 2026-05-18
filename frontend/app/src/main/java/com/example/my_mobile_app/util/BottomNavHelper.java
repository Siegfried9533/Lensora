package com.example.my_mobile_app.util;

import android.app.Activity;
import android.content.Intent;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.example.my_mobile_app.ui.notifications.NotificationsActivity;
import com.example.my_mobile_app.ui.profile.ProfileActivity;
import com.example.my_mobile_app.ui.transactions.TransactionsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires a {@link BottomNavigationView} to the main tab activities.
 *
 * Target Activity classes are added phase-by-phase as tabs land.
 */
public final class BottomNavHelper {

    private BottomNavHelper() {}

    public static void attachTo(final Activity activity,
                                BottomNavigationView nav,
                                int currentMenuId) {
        nav.setSelectedItemId(currentMenuId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentMenuId) return true;

            Class<?> target = targetForId(id);
            if (target == null) {
                // TODO: target activity not yet implemented in Phase 1.
                return false;
            }
            Intent intent = new Intent(activity, target)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.finish();
            return true;
        });
    }

    private static Class<?> targetForId(int id) {
        // TODO Phase 4+: return real Activity classes.
        if (id == R.id.nav_discover) {
            return HomeActivity.class;
        } else if (id == R.id.nav_transactions) {
            return TransactionsActivity.class;
        } else if (id == R.id.nav_notifications) {
            return NotificationsActivity.class;
        } else if (id == R.id.nav_profile) {
            return ProfileActivity.class;
        }
        return null;
    }
}
