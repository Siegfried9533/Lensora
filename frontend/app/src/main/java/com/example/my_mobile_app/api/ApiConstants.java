package com.example.my_mobile_app.api;

import com.example.my_mobile_app.BuildConfig;

/**
 * Network constants. BASE_URL is injected at build time from local.properties
 * (api.base.url). Falls back to the Android emulator loopback (10.0.2.2:8080).
 * Physical device: set api.base.url=http://<host-lan-ip>:8080/api/ in local.properties.
 */
public final class ApiConstants {
    public static final String BASE_URL = BuildConfig.BASE_URL;

    private ApiConstants() {}
}
