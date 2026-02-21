package com.example.uberproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.dto.response.MessageDTO;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_MINE   = 0;
    private static final int VIEW_TYPE_THEIRS = 1;

    private final List<MessageDTO> messages = new ArrayList<>();
    private final boolean isAdmin; // true = admin sent = ADMINISTRATOR type; false = user/driver

    public ChatMessagesAdapter(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setMessages(List<MessageDTO> newMessages) {
        messages.clear();
        if (newMessages != null) messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(MessageDTO msg) {
        // Avoid duplicate by id
        if (msg.getId() != null) {
            for (MessageDTO m : messages) {
                if (msg.getId().equals(m.getId())) return;
            }
        }
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        MessageDTO msg = messages.get(position);
        boolean isMine = isAdmin
                ? "ADMINISTRATOR".equals(msg.getUserType())
                : !"ADMINISTRATOR".equals(msg.getUserType());
        return isMine ? VIEW_TYPE_MINE : VIEW_TYPE_THEIRS;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_MINE
                ? R.layout.item_chat_message_mine
                : R.layout.item_chat_message_theirs;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageDTO msg = messages.get(position);
        holder.tvContent.setText(msg.getContent());
        holder.tvTime.setText(msg.getTimestamp() != null ? msg.getTimestamp() : "");
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime    = itemView.findViewById(R.id.tvMessageTime);
        }
    }
}