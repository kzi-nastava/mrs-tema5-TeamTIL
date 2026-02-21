package com.example.uberproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.dto.response.UserListItemDTO;
import java.util.ArrayList;
import java.util.List;

public class UserBlockAdapter extends RecyclerView.Adapter<UserBlockAdapter.ViewHolder> {

    public interface OnBlockActionListener {
        void onBlockAction(UserListItemDTO user, boolean currentlyBlocked);
    }

    private final Context context;
    private List<UserListItemDTO> allUsers = new ArrayList<>();
    private List<UserListItemDTO> filteredUsers = new ArrayList<>();
    private final OnBlockActionListener listener;

    public UserBlockAdapter(Context context, OnBlockActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setUsers(List<UserListItemDTO> users) {
        this.allUsers = new ArrayList<>(users);
        this.filteredUsers = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredUsers = new ArrayList<>(allUsers);
        } else {
            String q = query.toLowerCase().trim();
            filteredUsers = new ArrayList<>();
            for (UserListItemDTO u : allUsers) {
                String fullName = (u.getFirstName() + " " + u.getLastName()).toLowerCase();
                String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                String phone = u.getPhoneNumber() != null ? u.getPhoneNumber() : "";
                if (fullName.contains(q) || email.contains(q) || phone.contains(q)) {
                    filteredUsers.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user_block, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserListItemDTO user = filteredUsers.get(position);

        holder.tvName.setText(user.getFirstName() + " " + user.getLastName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

        boolean isBlocked = Boolean.TRUE.equals(user.getIsBlocked());

        if (isBlocked) {
            holder.blockedInfoLayout.setVisibility(View.VISIBLE);
            String reason = user.getBlockReason();
            holder.tvBlockReason.setText(reason != null && !reason.isEmpty() ? reason : "");
            holder.btnAction.setText("Unblock");
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_green);
        } else {
            holder.blockedInfoLayout.setVisibility(View.GONE);
            holder.btnAction.setText("Block");
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_red);
        }

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(holder.imgAvatar);
            holder.imgAvatar.setPadding(0, 0, 0, 0);
            holder.imgAvatar.setImageTintList(null);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        holder.btnAction.setOnClickListener(v -> listener.onBlockAction(user, isBlocked));
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvEmail, tvPhone, tvBlockReason;
        LinearLayout blockedInfoLayout;
        AppCompatButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPhone = itemView.findViewById(R.id.tvUserPhone);
            blockedInfoLayout = itemView.findViewById(R.id.blockedInfoLayout);
            tvBlockReason = itemView.findViewById(R.id.tvBlockReason);
            btnAction = itemView.findViewById(R.id.btnBlockAction);
        }
    }
}
