package com.example.my_mobile_app.api;

/**
 * Network constants. {@code BASE_URL} is hard-coded to the Android emulator
 * loopback alias. When testing on a physical device, replace 10.0.2.2 with
 * the host machine's LAN IP (see frontend/services/api/config.ts comments).
 */
public final class ApiConstants {
    //public static final String BASE_URL = "http://10.0.2.2:8080/api/";
    public static final String BASE_URL = "http://192.168.110.242:8080/api/";

    private ApiConstants() {}
}
