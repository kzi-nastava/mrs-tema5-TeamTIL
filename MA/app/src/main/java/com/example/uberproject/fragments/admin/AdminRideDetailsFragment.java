package com.example.uberproject.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.model.Ride;

public class AdminRideDetailsFragment extends Fragment {

    private static final String ARG_RIDE = "arg_ride";

    private Ride ride;
    private LinearLayout driversContainer;
    private LinearLayout issuesContainer;
    private TextView addressText;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvDuration;
    private TextView tvDistance;
    private TextView tvPrice;
    private Button btnStatus;

    public static AdminRideDetailsFragment newInstance(Ride ride) {
        AdminRideDetailsFragment fragment = new AdminRideDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, ride);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_user_ride_history_details, container, false);

        if (getArguments() != null) {
            ride = (Ride) getArguments().getSerializable(ARG_RIDE);
        }

        driversContainer = view.findViewById(R.id.driversContainer);
        issuesContainer = view.findViewById(R.id.issuesContainer);
        addressText = view.findViewById(R.id.tvRideAddress);
        tvStartTime = view.findViewById(R.id.tvStartTime);
        tvEndTime = view.findViewById(R.id.tvEndTime);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvPrice = view.findViewById(R.id.tvPrice);
        btnStatus = view.findViewById(R.id.btnRideStatus);

        populateRideData(inflater);

        return view;
    }

    private void populateRideData(LayoutInflater inflater) {
        if (ride == null) return;

        addressText.setText(
                ride.getFrom() + " → " + ride.getTo()
        );

        // MOCK DRIVER
        addDriver(inflater, "John Pork", "+381 125 456 789");

        // MOCK ISSUES
        addIssue(inflater, "Passenger was late");
        addIssue(inflater, "Dropoff location changed");

        tvStartTime.setText("14 Mar 2025, 15:32");
        tvEndTime.setText("16:02");

        tvDuration.setText("30min");
        tvDistance.setText("8.2 km");

        tvPrice.setText(ride.getPrice());

        btnStatus.setText(ride.getStatus());

        if ("Finished".equalsIgnoreCase(ride.getStatus())) {
            btnStatus.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_green, null));
        } else if ("Canceled".equalsIgnoreCase(ride.getStatus())) {
            btnStatus.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_red, null));
        }
    }

    private void addDriver(LayoutInflater inflater, String name, String phone) {
        View driverView = inflater.inflate(
                R.layout.item_driver,
                driversContainer,
                false
        );

        ((TextView) driverView.findViewById(R.id.tvDriverName)).setText(name);
        ((TextView) driverView.findViewById(R.id.tvDriverPhone)).setText(phone);

        driversContainer.addView(driverView);
    }

    private void addIssue(LayoutInflater inflater, String issue) {
        View issueView = inflater.inflate(
                R.layout.item_issue,
                issuesContainer,
                false
        );

        ((TextView) issueView.findViewById(R.id.tvIssueText)).setText(issue);

        issuesContainer.addView(issueView);
    }
}
