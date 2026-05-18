package com.example.my_mobile_app.ui.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.api.dto.ChangePasswordRequest;
import com.example.my_mobile_app.ui.BaseActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends BaseActivity {

    private EditText edtOld, edtNew, edtConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_change_password);

        ImageButton btnBack = findViewById(R.id.btn_back);
        edtOld = findViewById(R.id.edt_old);
        edtNew = findViewById(R.id.edt_new);
        edtConfirm = findViewById(R.id.edt_confirm);
        Button btnSubmit = findViewById(R.id.btn_submit);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String oldPwd = edtOld.getText().toString().trim();
        String newPwd = edtNew.getText().toString().trim();
        String confirm = edtConfirm.getText().toString().trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            showError(getString(R.string.error_required_field));
            return;
        }
        if (newPwd.length() < 6) {
            showError(getString(R.string.error_password_too_short));
            return;
        }
        if (!newPwd.equals(confirm)) {
            showError(getString(R.string.error_password_mismatch));
            return;
        }

        showLoading();
        ApiClient.get(this).create(AuthService.class)
                .changePassword(new ChangePasswordRequest(oldPwd, newPwd))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                                      @NonNull Response<ApiResponse<Void>> response) {
                        hideLoading();
                        ApiResponse<Void> body = response.body();
                        if (body != null && body.success) {
                            showSuccess(getString(R.string.change_password_success));
                            finish();
                        } else {
                            String msg = body != null && body.message != null
                                    ? body.message : getString(R.string.error_generic);
                            showError(msg);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call,
                                                    @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_network));
                    }
                });
    }
}
