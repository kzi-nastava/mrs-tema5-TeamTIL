package com.example.uberproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.dto.response.PanicResponseDTO;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PanicNotificationAdapter extends RecyclerView.Adapter<PanicNotificationAdapter.ViewHolder> {

    public interface OnHandleClickListener {
        void onHandleClick(PanicResponseDTO panic);
    }

    private final List<PanicResponseDTO> panics;
    private final OnHandleClickListener handleClickListener;

    public PanicNotificationAdapter(List<PanicResponseDTO> panics, OnHandleClickListener handleClickListener) {
        this.panics = panics;
        this.handleClickListener = handleClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_panic_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PanicResponseDTO panic = panics.get(position);

        // Status
        boolean handled = Boolean.TRUE.equals(panic.getHandled());
        if (handled) {
            holder.tvStatus.setText("✅ HANDLED");
            holder.tvStatus.setTextColor(0xFF4CAF50);
            holder.btnHandle.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("🚨 ACTIVE");
            holder.tvStatus.setTextColor(0xFFFF4444);
            holder.btnHandle.setVisibility(View.VISIBLE);
        }

        // Ride ID
        holder.tvRideId.setText(panic.getRideId() != null ? String.valueOf(panic.getRideId()) : "—");

        // Vehicle
        holder.tvVehicle.setText(panic.getVehicleName() != null ? panic.getVehicleName() : "—");

        // License plate
        holder.tvPlate.setText(panic.getVehicleLicensePlate() != null ? panic.getVehicleLicensePlate() : "—");

        // Location
        holder.tvLocation.setText(panic.getLocationAddress() != null ? panic.getLocationAddress() : "—");

        // Reported by
        holder.tvReportedBy.setText(panic.getReportedBy() != null ? panic.getReportedBy() : "—");

        // Timestamp
        String ts = panic.getTimestamp();
        if (ts != null && ts.length() > 10) {
            holder.tvTimestamp.setText(ts.substring(0, 16).replace("T", " "));
        } else {
            holder.tvTimestamp.setText(ts != null ? ts : "—");
        }

        holder.btnHandle.setOnClickListener(v -> {
            if (handleClickListener != null) {
                handleClickListener.onHandleClick(panic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return panics.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvRideId, tvVehicle, tvPlate, tvLocation, tvReportedBy, tvTimestamp;
        MaterialButton btnHandle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvPanicStatus);
            tvRideId = itemView.findViewById(R.id.tvPanicRideId);
            tvVehicle = itemView.findViewById(R.id.tvPanicVehicle);
            tvPlate = itemView.findViewById(R.id.tvPanicPlate);
            tvLocation = itemView.findViewById(R.id.tvPanicLocation);
            tvReportedBy = itemView.findViewById(R.id.tvPanicReportedBy);
            tvTimestamp = itemView.findViewById(R.id.tvPanicTimestamp);
            btnHandle = itemView.findViewById(R.id.btnHandlePanic);
        }
    }
}

