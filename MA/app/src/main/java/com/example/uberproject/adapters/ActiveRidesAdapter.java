package com.example.uberproject.adapters;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.dto.response.ActiveRideAdminDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveRidesAdapter extends RecyclerView.Adapter<ActiveRidesAdapter.ViewHolder> {

    private List<ActiveRideAdminDTO> rides = new ArrayList<>();
    private OnRideClickListener listener;

    public interface OnRideClickListener {
        void onRideClick(ActiveRideAdminDTO ride);
    }

    public ActiveRidesAdapter(OnRideClickListener listener) {
        this.listener = listener;
    }

    public void setRides(List<ActiveRideAdminDTO> newRides) {
        this.rides = new ArrayList<>(newRides);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_ride, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveRideAdminDTO ride = rides.get(position);

        holder.tvDriverName.setText(ride.getDriverFullName().trim());
        holder.tvDriverRating.setText(ride.getFormattedRating());
        holder.tvVehicleModel.setText(ride.getVehicleModel() != null ? ride.getVehicleModel() : "");
        holder.tvDriverPhone.setText(ride.getDriverPhone() != null ? ride.getDriverPhone() : "");
        holder.tvStartAddress.setText(ride.getStartAddress() != null ? ride.getStartAddress() : "");
        holder.tvEndAddress.setText(ride.getEndAddress() != null ? ride.getEndAddress() : "");

        // Time: extract HH:mm part from "dd MMM yyyy, HH:mm"
        String timeDisplay = extractTime(ride.getStartTime());
        holder.tvRideTime.setText(timeDisplay);

        // Status badge
        if (ride.isInProgress()) {
            holder.tvRideStatus.setText("ongoing");
            holder.tvRideStatus.setBackgroundResource(R.drawable.status_in_progress_bg);
        } else {
            holder.tvRideStatus.setText("upcoming");
            holder.tvRideStatus.setBackgroundResource(R.drawable.status_upcoming_bg);
        }

        // Driver avatar
        loadAvatar(holder.ivDriverAvatar, ride.getDriverProfilePicture());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRideClick(ride);
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    private String extractTime(String dateTime) {
        if (dateTime == null) return "";
        // format: "dd MMM yyyy, HH:mm" → get HH:mm
        int commaIdx = dateTime.lastIndexOf(", ");
        if (commaIdx >= 0 && commaIdx + 2 < dateTime.length()) {
            return dateTime.substring(commaIdx + 2);
        }
        return dateTime;
    }

    private void loadAvatar(ImageView imageView, String picData) {
        if (picData == null || picData.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_person_placeholder);
            return;
        }
        if (picData.startsWith("http")) {
            Glide.with(imageView.getContext())
                    .load(picData)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(imageView);
        } else {
            // Base64
            try {
                byte[] bytes = Base64.decode(picData, Base64.DEFAULT);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_person_placeholder);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDriverAvatar;
        TextView tvDriverName, tvDriverRating, tvVehicleModel, tvDriverPhone;
        TextView tvStartAddress, tvEndAddress, tvRideTime, tvRideStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDriverAvatar  = itemView.findViewById(R.id.ivDriverAvatar);
            tvDriverName    = itemView.findViewById(R.id.tvDriverName);
            tvDriverRating  = itemView.findViewById(R.id.tvDriverRating);
            tvVehicleModel  = itemView.findViewById(R.id.tvVehicleModel);
            tvDriverPhone   = itemView.findViewById(R.id.tvDriverPhone);
            tvStartAddress  = itemView.findViewById(R.id.tvStartAddress);
            tvEndAddress    = itemView.findViewById(R.id.tvEndAddress);
            tvRideTime      = itemView.findViewById(R.id.tvRideTime);
            tvRideStatus    = itemView.findViewById(R.id.tvRideStatus);
        }
    }
}