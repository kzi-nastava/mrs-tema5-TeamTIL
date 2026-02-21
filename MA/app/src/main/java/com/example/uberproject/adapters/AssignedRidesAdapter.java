package com.example.uberproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.dto.response.AssignedRideDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignedRidesAdapter extends RecyclerView.Adapter<AssignedRidesAdapter.RideViewHolder> {

    private List<AssignedRideDTO> rides = new ArrayList<>();
    private int expandedPosition = -1;
    private OnRideActionListener listener;

    public interface OnRideActionListener {
        void onOpenRide(AssignedRideDTO ride);
        void onStartRide(AssignedRideDTO ride);
        void onStopRide(AssignedRideDTO ride);
        void onEndRide(AssignedRideDTO ride);
        void onCancelRide(AssignedRideDTO ride);
    }

    public AssignedRidesAdapter(OnRideActionListener listener) {
        this.listener = listener;
    }

    public void setRides(List<AssignedRideDTO> rides) {
        this.rides = rides;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assigned_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        AssignedRideDTO ride = rides.get(position);
        boolean isExpanded = position == expandedPosition;

        // Set ride time and status
        String timeText = formatDateTime(ride.getStartTime());
        String statusText = ride.getStatus().equals("IN_PROGRESS") ? " - ongoing" :
                           ride.getStatus().equals("REQUESTED") ? " - requested" : "";
        holder.tvRideTime.setText(timeText + statusText);

        // Set locations
        holder.tvRideLocations.setText(ride.getStartLocation() + " → " + ride.getEndLocation());

        // Set status badge
        if (ride.getStatus().equals("IN_PROGRESS")) {
            holder.tvRideStatus.setText("In progress");
            holder.tvRideStatus.setBackgroundResource(R.drawable.status_in_progress_bg);
        } else if (ride.getStatus().equals("UPCOMING")) {
            holder.tvRideStatus.setText("Upcoming");
            holder.tvRideStatus.setBackgroundResource(R.drawable.status_upcoming_bg);
        } else if (ride.getStatus().equals("REQUESTED")) {
            holder.tvRideStatus.setText("Requested");
            holder.tvRideStatus.setBackgroundResource(R.drawable.status_upcoming_bg); // Koristi isti stil kao UPCOMING
        }

        // Set expanded details
        holder.expandedDetailsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            // Display passenger name and email from database
            String passengerName = (ride.getPassengerFirstName() != null ? ride.getPassengerFirstName() : "")
                    + " " + (ride.getPassengerLastName() != null ? ride.getPassengerLastName() : "");
            if (passengerName.trim().isEmpty()) {
                holder.tvPassengerEmail.setText(ride.getPassengerEmail() != null ? ride.getPassengerEmail() : ride.getAccountEmail());
            } else {
                holder.tvPassengerEmail.setText(passengerName.trim());
            }

            // Load passenger profile picture
            if (ride.getPassengerProfilePictureUrl() != null && !ride.getPassengerProfilePictureUrl().isEmpty()) {
                Glide.with(holder.ivPassengerPhoto.getContext())
                        .load(ride.getPassengerProfilePictureUrl())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(holder.ivPassengerPhoto);
            }

            holder.tvEstimatedEndTime.setText(formatTime(ride.getEstimatedEndTime()));
            holder.tvDuration.setText(String.format(Locale.getDefault(), "%.0f min", ride.getDuration() != null ? ride.getDuration() : 0));
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%.0f RSD", ride.getPrice() != null ? ride.getPrice() : 0));
            holder.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", ride.getDistance() != null ? ride.getDistance() : 0));

            // Show appropriate buttons based on status
            if (ride.getStatus().equals("IN_PROGRESS")) {
                holder.actionButtonsLayout.setVisibility(View.VISIBLE);
                holder.upcomingButtonsLayout.setVisibility(View.GONE);
            } else if (ride.getStatus().equals("UPCOMING") || ride.getStatus().equals("REQUESTED")) {
                holder.actionButtonsLayout.setVisibility(View.GONE);
                holder.upcomingButtonsLayout.setVisibility(View.VISIBLE);
            }
        }

        // Handle card click to expand/collapse
        holder.rideCard.setOnClickListener(v -> {
            int previousExpandedPosition = expandedPosition;
            if (isExpanded) {
                expandedPosition = -1;
            } else {
                expandedPosition = holder.getAdapterPosition();
            }

            if (previousExpandedPosition != -1) {
                notifyItemChanged(previousExpandedPosition);
            }
            notifyItemChanged(holder.getAdapterPosition());
        });

        // Set button click listeners
        holder.btnOpenRide.setOnClickListener(v -> {
            if (listener != null) listener.onOpenRide(ride);
        });

        holder.btnStopRide.setOnClickListener(v -> {
            if (listener != null) listener.onStopRide(ride);
        });

        holder.btnEndRide.setOnClickListener(v -> {
            if (listener != null) listener.onEndRide(ride);
        });

        holder.btnStartRide.setOnClickListener(v -> {
            if (listener != null) listener.onStartRide(ride);
        });

        holder.btnCancelRide.setOnClickListener(v -> {
            if (listener != null) listener.onCancelRide(ride);
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    private String formatDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    private String formatTime(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rideCard;
        TextView tvRideTime, tvRideStatus, tvRideLocations;
        LinearLayout expandedDetailsLayout, actionButtonsLayout, upcomingButtonsLayout;
        ImageView ivPassengerPhoto;
        TextView tvPassengerEmail, tvEstimatedEndTime, tvDuration, tvPrice, tvDistance;
        Button btnOpenRide, btnStopRide, btnEndRide, btnStartRide, btnCancelRide;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            rideCard = itemView.findViewById(R.id.rideCard);
            tvRideTime = itemView.findViewById(R.id.tvRideTime);
            tvRideStatus = itemView.findViewById(R.id.tvRideStatus);
            tvRideLocations = itemView.findViewById(R.id.tvRideLocations);
            expandedDetailsLayout = itemView.findViewById(R.id.expandedDetailsLayout);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            upcomingButtonsLayout = itemView.findViewById(R.id.upcomingButtonsLayout);
            ivPassengerPhoto = itemView.findViewById(R.id.ivPassengerPhoto);
            tvPassengerEmail = itemView.findViewById(R.id.tvPassengerEmail);
            tvEstimatedEndTime = itemView.findViewById(R.id.tvEstimatedEndTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnOpenRide = itemView.findViewById(R.id.btnOpenRide);
            btnStopRide = itemView.findViewById(R.id.btnStopRide);
            btnEndRide = itemView.findViewById(R.id.btnEndRide);
            btnStartRide = itemView.findViewById(R.id.btnStartRide);
            btnCancelRide = itemView.findViewById(R.id.btnCancelRide);
        }
    }
}

