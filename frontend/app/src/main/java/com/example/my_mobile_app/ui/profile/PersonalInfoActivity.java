package com.example.my_mobile_app.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.api.dto.UpdateAvatarRequest;
import com.example.my_mobile_app.model.User;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.util.UserManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalInfoActivity extends BaseActivity {

    private ImageView imgAvatar;
    private EditText edtName, edtEmail;
    private Uri pickedAvatarUri;

    // TODO: backend expects an avatar URL. Image picker returns a local content URI;
    // a real file-upload endpoint is needed for proper avatar updates.
    private final ActivityResultLauncher<PickVisualMediaRequest> picker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) return;
                pickedAvatarUri = uri;
                Glide.with(this).load(uri).circleCrop().into(imgAvatar);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_personal_info);

        ImageButton btnBack = findViewById(R.id.btn_back);
        imgAvatar = findViewById(R.id.img_avatar);
        TextView txtChange = findViewById(R.id.txt_change_avatar);
        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        Button btnSave = findViewById(R.id.btn_save);

        User user = UserManager.getUser(this);
        if (user != null) {
            edtName.setText(user.userName == null ? "" : user.userName);
            edtEmail.setText(user.email == null ? "" : user.email);
            if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                Glide.with(this).load(user.avatarUrl).circleCrop().into(imgAvatar);
            }
        }

        btnBack.setOnClickListener(v -> finish());
        txtChange.setOnClickListener(v -> pickAvatar());
        imgAvatar.setOnClickListener(v -> pickAvatar());
        btnSave.setOnClickListener(v -> save());
    }

    private void pickAvatar() {
        picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void save() {
        if (pickedAvatarUri == null) {
            showSuccess(getString(R.string.personal_info_saved));
            setResult(Activity.RESULT_OK);
            return;
        }
        showLoading();
        UpdateAvatarRequest req = new UpdateAvatarRequest(pickedAvatarUri.toString());
        ApiClient.get(this).create(AuthService.class).updateAvatar(req)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<User>> call,
                                                      @NonNull Response<ApiResponse<User>> response) {
                        hideLoading();
                        ApiResponse<User> body = response.body();
                        if (body != null && body.success && body.data != null) {
                            UserManager.saveUser(PersonalInfoActivity.this, body.data);
                            showSuccess(getString(R.string.personal_info_avatar_updated));
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            showError(getString(R.string.error_generic));
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<User>> call,
                                                    @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_network));
                    }
                });
    }
}
