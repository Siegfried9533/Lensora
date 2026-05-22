package com.example.my_mobile_app.ui.chatbot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiConstants;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.ChatbotService;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.model.ChatMessage;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.example.my_mobile_app.util.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;

/** Chatbot tab: streams POST /chatbot/chat and falls back to /chatbot/chat-sync. */
public class ChatbotActivity extends BaseActivity {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private final List<ChatMessage> messages = new ArrayList<>();

    private RecyclerView rvMessages;
    private TextView txtEmpty;
    private EditText inputMessage;
    private ImageButton btnSend;
    private ChatMessageAdapter adapter;
    private Call streamCall;
    private boolean isSending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rvMessages = findViewById(R.id.rv_messages);
        txtEmpty = findViewById(R.id.txt_empty);
        inputMessage = findViewById(R.id.input_message);
        btnSend = findViewById(R.id.btn_send);
        ImageButton btnBack = findViewById(R.id.btn_back);

        messages.add(new ChatMessage("assistant", getString(R.string.chatbot_welcome)));
        adapter = new ChatMessageAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);
        rvMessages.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        BottomNavHelper.attachTo(this, bottomNav, 0);

        btnBack.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, HomeActivity.class)
                    .setFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        btnSend.setOnClickListener(v -> send());
        renderEmpty();
    }

    private void send() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty() || isSending) return;

        inputMessage.setText("");
        hideKeyboard();
        messages.add(new ChatMessage("user", text));
        messages.add(new ChatMessage("assistant", ""));
        adapter.notifyItemRangeInserted(messages.size() - 2, 2);
        rvMessages.scrollToPosition(messages.size() - 1);
        renderEmpty();
        setSending(true);

        streamChat();
    }

    private void streamChat() {
        OkHttpClient client = ApiClient.getHttpClient(this);
        Map<String, List<ChatMessage>> body = new HashMap<>();
        body.put("messages", messagesForRequest());

        Request request = new Request.Builder()
                .url(ApiConstants.BASE_URL + "chatbot/chat")
                .post(RequestBody.create(gson.toJson(body), JSON))
                .build();

        streamCall = client.newCall(request);
        streamCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (call.isCanceled()) return;
                main.post(() -> fallbackNonStream());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful() || response.body() == null) {
                    main.post(() -> fallbackNonStream());
                    response.close();
                    return;
                }
                try (Response ignored = response) {
                    String line;
                    while ((line = response.body().source().readUtf8Line()) != null) {
                        String chunk = parseChunk(line);
                        if (chunk == null || chunk.isEmpty()) continue;
                        main.post(() -> appendBotChunk(chunk));
                    }
                    main.post(() -> setSending(false));
                } catch (Exception e) {
                    main.post(() -> fallbackNonStream());
                }
            }
        });
    }

    private List<ChatMessage> messagesForRequest() {
        List<ChatMessage> out = new ArrayList<>();
        for (ChatMessage message : messages) {
            if (message.content == null || message.content.isEmpty()) continue;
            out.add(message);
        }
        return out;
    }

    private String parseChunk(String raw) {
        String line = raw == null ? "" : raw.trim();
        if (line.isEmpty()) return null;
        if (line.startsWith("data:")) line = line.substring(5).trim();
        try {
            JsonObject json = gson.fromJson(line, JsonObject.class);
            if (json == null) return null;
            if (json.has("error")) {
                main.post(() -> showError(json.get("error").getAsString()));
                return null;
            }
            if (json.has("done") && json.get("done").getAsBoolean()) return null;
            if (json.has("message") && json.get("message").isJsonObject()) {
                JsonObject message = json.getAsJsonObject("message");
                if (message.has("content")) return message.get("content").getAsString();
            }
            if (json.has("content")) return json.get("content").getAsString();
        } catch (Exception ignored) {
            return line;
        }
        return null;
    }

    private void appendBotChunk(String chunk) {
        if (messages.isEmpty()) return;
        ChatMessage bot = messages.get(messages.size() - 1);
        bot.content = (bot.content == null ? "" : bot.content) + chunk;
        adapter.notifyItemChanged(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    private void fallbackNonStream() {
        Retrofit retrofit = ApiClient.get(this);
        Map<String, List<ChatMessage>> body = new HashMap<>();
        body.put("messages", messagesForRequest());
        retrofit.create(ChatbotService.class).chatNonStream(body)
                .enqueue(new retrofit2.Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<ApiResponse<String>> call,
                                           @NonNull retrofit2.Response<ApiResponse<String>> response) {
                        ApiResponse<String> res = response.body();
                        if (res != null && res.success && res.data != null) {
                            setLastBotMessage(res.data);
                        } else {
                            setLastBotMessage(getString(R.string.chatbot_fallback));
                        }
                        setSending(false);
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<ApiResponse<String>> call,
                                          @NonNull Throwable t) {
                        setLastBotMessage(getString(R.string.error_chatbot_connection));
                        setSending(false);
                    }
                });
    }

    private void setLastBotMessage(String content) {
        if (messages.isEmpty()) return;
        messages.get(messages.size() - 1).content = content;
        adapter.notifyItemChanged(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    private void setSending(boolean sending) {
        isSending = sending;
        btnSend.setEnabled(!sending);
        inputMessage.setEnabled(!sending);
    }

    private void renderEmpty() {
        txtEmpty.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(inputMessage.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        if (streamCall != null) streamCall.cancel();
        super.onDestroy();
    }
}
