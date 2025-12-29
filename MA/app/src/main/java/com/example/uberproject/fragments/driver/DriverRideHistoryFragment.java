package com.example.uberproject.fragments.driver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.adapters.RideAdapter;
import com.example.uberproject.models.Ride;

import java.util.ArrayList;
import java.util.List;

public class DriverRideHistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_ride_history, container, false);

        RecyclerView ridesRecyclerView = view.findViewById(R.id.ridesRecyclerView);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Mock data
        List<Ride> mockRides = new ArrayList<>();
        mockRides.add(new Ride("Stražilovska 32", "Bulevar Kralja Petra 7", "1,050 RSD", "Completed", "14 Mar 2025, 15:32 - 16:05"));
        mockRides.add(new Ride("Novi Sad", "Beograd", "1,250 RSD", "Completed", "14 Mar 2025, 14:32 - 15:05"));
        mockRides.add(new Ride("Zemun", "Novi Beograd", "1,670 RSD", "Completed", "14 Mar 2025, 12:32 - 13:05"));

        RideAdapter rideAdapter = new RideAdapter(mockRides);
        ridesRecyclerView.setAdapter(rideAdapter);

        return view;
    }
}
