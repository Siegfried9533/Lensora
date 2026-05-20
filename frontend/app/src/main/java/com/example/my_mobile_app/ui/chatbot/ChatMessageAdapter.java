package com.example.my_mobile_app.ui.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.model.ChatMessage;

import java.util.List;

/** Two-bubble adapter for user and assistant chat messages. */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.VH> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_BOT = 2;

    private final List<ChatMessage> messages;

    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return "user".equals(messages.get(position).role) ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_USER ? R.layout.item_message_user : R.layout.item_message_bot;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatMessage message = messages.get(position);
        holder.txtContent.setText(message.content);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView txtContent;

        VH(@NonNull View itemView) {
            super(itemView);
            txtContent = itemView.findViewById(R.id.txt_message_content);
        }
    }
}
