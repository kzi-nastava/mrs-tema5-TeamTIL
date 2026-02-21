package com.example.uberproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.dto.response.ChatDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final List<ChatDTO> allChats = new ArrayList<>();
    private final List<ChatDTO> visibleChats = new ArrayList<>();
    private final Map<Integer, Integer> unreadCounts = new HashMap<>();
    private Integer selectedChatId = null;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatDTO chat);
    }

    public ChatListAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setChats(List<ChatDTO> newChats) {
        allChats.clear();
        if (newChats != null) allChats.addAll(newChats);
        visibleChats.clear();
        visibleChats.addAll(allChats);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        visibleChats.clear();
        if (query == null || query.trim().isEmpty()) {
            visibleChats.addAll(allChats);
        } else {
            String q = query.toLowerCase(Locale.ROOT).trim();
            for (ChatDTO c : allChats) {
                if (c.getUserFullName().toLowerCase(Locale.ROOT).contains(q)
                        || (c.getUserEmail() != null
                        && c.getUserEmail().toLowerCase(Locale.ROOT).contains(q))) {
                    visibleChats.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setSelectedChatId(Integer id) {
        selectedChatId = id;
        notifyDataSetChanged();
    }

    public void incrementUnread(int chatId) {
        int current = unreadCounts.containsKey(chatId) ? unreadCounts.get(chatId) : 0;
        unreadCounts.put(chatId, current + 1);
        notifyDataSetChanged();
    }

    public void clearUnread(int chatId) {
        unreadCounts.remove(chatId);
        notifyDataSetChanged();
    }

    public void updateChatPreview(int chatId, String content, String time, String userType) {
        updateInList(allChats, chatId, content, time, userType);
        updateInList(visibleChats, chatId, content, time, userType);
        notifyDataSetChanged();
    }

    private void updateInList(List<ChatDTO> list, int chatId,
                              String content, String time, String userType) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(chatId)) {
                ChatDTO chat = list.remove(i);
                chat.setLastMessageContent(content);
                chat.setLastMessageTime(time);
                chat.setLastMessageUserType(userType);
                list.add(0, chat);
                return;
            }
        }
    }

    public void addChatAtTop(ChatDTO chat) {
        allChats.add(0, chat);
        visibleChats.add(0, chat);
        notifyDataSetChanged();
    }

    public boolean containsChat(int chatId) {
        for (ChatDTO c : allChats) {
            if (c.getId().equals(chatId)) return true;
        }
        return false;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatDTO chat = visibleChats.get(position);

        holder.tvUserName.setText(chat.getUserFullName());
        holder.tvUserRole.setText(chat.getRoleLabel());

        if (chat.getLastMessageContent() != null && !chat.getLastMessageContent().isEmpty()) {
            String prefix = "ADMINISTRATOR".equals(chat.getLastMessageUserType())
                    ? "You" : chat.getUserFirstName();
            String preview = chat.getLastMessageContent();
            if (preview.length() > 32) preview = preview.substring(0, 32) + "…";
            holder.tvLastMessage.setText(prefix + ": " + preview);
            holder.tvLastMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvLastMessage.setVisibility(View.GONE);
        }

        holder.tvLastTime.setText(
                chat.getLastMessageTime() != null ? chat.getLastMessageTime() : "");

        int unread = unreadCounts.containsKey(chat.getId()) ? unreadCounts.get(chat.getId()) : 0;
        if (unread > 0) {
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            holder.tvUnreadBadge.setText(String.valueOf(unread));
        } else {
            holder.tvUnreadBadge.setVisibility(View.GONE);
        }

        boolean isSelected = chat.getId().equals(selectedChatId);
        holder.itemView.setBackgroundResource(isSelected
                ? R.drawable.bg_chat_item_selected
                : R.drawable.bg_chat_item_normal);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(chat);
        });
    }

    @Override
    public int getItemCount() { return visibleChats.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRole, tvLastMessage, tvLastTime, tvUnreadBadge;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName    = itemView.findViewById(R.id.tvChatUserName);
            tvUserRole    = itemView.findViewById(R.id.tvChatUserRole);
            tvLastMessage = itemView.findViewById(R.id.tvChatLastMessage);
            tvLastTime    = itemView.findViewById(R.id.tvChatLastTime);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
        }
    }
}