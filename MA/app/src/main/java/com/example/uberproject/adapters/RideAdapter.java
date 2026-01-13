package com.example.uberproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.models.Ride;

import java.util.ArrayList;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rides;

    public RideAdapter(List<Ride> rides) {
        this.rides = new ArrayList<>(rides);
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rides.get(position);
        holder.tvRoute.setText(ride.getFrom() + " → " + ride.getTo());
        holder.tvPrice.setText(ride.getPrice());
        holder.tvStatus.setText(ride.getStatus());
        holder.tvDateTime.setText(ride.getDateTime());
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public void setRides(List<Ride> newRides) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return rides.size();
            }

            @Override
            public int getNewListSize() {
                return newRides.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return rides.get(oldItemPosition).getId().equals(newRides.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return rides.get(oldItemPosition).equals(newRides.get(newItemPosition));
            }
        });

        rides.clear();
        rides.addAll(newRides);
        diffResult.dispatchUpdatesTo(this);
    }


    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoute, tvPrice, tvStatus, tvDateTime;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDateTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
