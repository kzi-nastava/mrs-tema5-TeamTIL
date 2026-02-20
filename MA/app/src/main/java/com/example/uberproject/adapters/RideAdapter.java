package com.example.uberproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.model.Ride;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rides;
    private OnRideClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    // cuva routeIdjeve koji su oznaceni kao omiljeni
    private Set<Integer> favoriteRouteIds = new HashSet<>();

    public RideAdapter(List<Ride> rides, OnRideClickListener listener) {
        this.rides = new ArrayList<>(rides);
        this.listener = listener;
    }

    public void setFavoriteListener(OnFavoriteClickListener favoriteListener) {
        this.favoriteListener = favoriteListener;
    }

    public void setFavoriteRouteIds(Set<Integer> ids) {
        this.favoriteRouteIds = new HashSet<>(ids);
        notifyDataSetChanged();
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

        // Status background
        if ("Canceled".equalsIgnoreCase(ride.getStatus()) || "CANCELED".equalsIgnoreCase(ride.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_canceled);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
        }

        // Panic badge
        if (ride.getPanicSent() != null && ride.getPanicSent()) {
            holder.tvPanicBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvPanicBadge.setVisibility(View.GONE);
        }

        // ---- ZVEZDICA ----
        Integer routeId = ride.getRouteId();
        if (routeId != null) {
            holder.ivFavoriteStar.setVisibility(View.VISIBLE);

            boolean isFav = favoriteRouteIds.contains(routeId);
            // puna zvezdica = omiljena, prazna = nije
            holder.ivFavoriteStar.setImageResource(
                    isFav ? R.drawable.ic_star_filled2 : R.drawable.ic_star_outline2
            );

            holder.ivFavoriteStar.setOnClickListener(v -> {
                if (favoriteListener != null) {
                    boolean currentlyFav = favoriteRouteIds.contains(routeId);
                    if (currentlyFav) {
                        favoriteRouteIds.remove(routeId);
                        holder.ivFavoriteStar.setImageResource(R.drawable.ic_star_outline2);
                        favoriteListener.onRemoveFromFavorites(routeId);
                    } else {
                        favoriteRouteIds.add(routeId);
                        holder.ivFavoriteStar.setImageResource(R.drawable.ic_star_filled2);
                        favoriteListener.onAddToFavorites(routeId);
                    }
                }
            });
        } else {
            // nema routeId - sakrij zvezdicu
            holder.ivFavoriteStar.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRideClick(ride);
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public void setRides(List<Ride> newRides) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return rides.size(); }
            @Override public int getNewListSize() { return newRides.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return rides.get(oldPos).getId() != null
                        && rides.get(oldPos).getId().equals(newRides.get(newPos).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return rides.get(oldPos).equals(newRides.get(newPos));
            }
        });
        rides.clear();
        rides.addAll(newRides);
        diffResult.dispatchUpdatesTo(this);
    }

    // ---- INTERFACES ----
    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    public interface OnFavoriteClickListener {
        void onAddToFavorites(Integer routeId);
        void onRemoveFromFavorites(Integer routeId);
    }

    // ---- VIEW HOLDER ----
    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoute, tvPrice, tvStatus, tvDateTime, tvPanicBadge;
        ImageView ivFavoriteStar;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute        = itemView.findViewById(R.id.tvRoute);
            tvPrice        = itemView.findViewById(R.id.tvPrice);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
            tvDateTime     = itemView.findViewById(R.id.tvTime);
            tvPanicBadge   = itemView.findViewById(R.id.tvPanicBadge);
            ivFavoriteStar = itemView.findViewById(R.id.ivFavoriteStar);
        }
    }
}
