package com.example.my_mobile_app.api;

import android.content.Context;

import com.example.my_mobile_app.BuildConfig;
import com.example.my_mobile_app.util.TokenManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit holder. Reads the JWT token via {@link TokenManager}
 * on every request, so callers don't need to pass it.
 */
public final class ApiClient {

    private static volatile Retrofit retrofit;
    private static volatile OkHttpClient httpClient;

    private ApiClient() {}

    public static Retrofit get(Context context) {
        Retrofit local = retrofit;
        if (local == null) {
            synchronized (ApiClient.class) {
                local = retrofit;
                if (local == null) {
                    local = build(context.getApplicationContext());
                    retrofit = local;
                }
            }
        }
        return local;
    }

    public static OkHttpClient getHttpClient(Context context) {
        if (httpClient == null) {
            get(context);
        }
        return httpClient;
    }

    private static Retrofit build(Context appContext) {
        OkHttpClient.Builder http = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(appContext));

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            http.addInterceptor(logging);
        }

        httpClient = http.build();
        return new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /** Adds Bearer token + Content-Type when missing. */
    private static final class AuthInterceptor implements Interceptor {
        private final Context appContext;

        AuthInterceptor(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder b = original.newBuilder();

            if (original.header("Content-Type") == null) {
                b.header("Content-Type", "application/json; charset=utf-8");
            }
            b.header("Accept", "application/json");
            b.header("Accept-Charset", "UTF-8");
            String token = TokenManager.getToken(appContext);
            if (token != null && !token.isEmpty() && original.header("Authorization") == null) {
                b.header("Authorization", "Bearer " + token);
            }
            return chain.proceed(b.build());
        }
    }
}
