package com.example.uberproject.fragments.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.adapters.RideAdapter;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.response.RideHistoryResponseDTO;
import com.example.uberproject.model.Ride;
import com.example.uberproject.utils.TokenManager;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRideHistoryFragment extends Fragment {

    private AutoCompleteTextView etFromDate, etToDate;
    private AppCompatButton btnApplyFilters;
    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> allRides;
    private List<RideHistoryResponseDTO> rideHistoryData;
    private Chip chipLast7Days, chipLastMonth, chipCompletedOnly, chipCanceledOnly, chipPanicOnly, chipAll;
    private android.widget.ProgressBar loadingProgressBar;
    private static final String TAG = "AdminRideHistoryFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_ride_history, container, false);

        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        etFromDate = view.findViewById(R.id.etFromDate);
        etToDate = view.findViewById(R.id.etToDate);
        setupDatePickers();

        LinearLayout fromLayout = view.findViewById(R.id.fromLayout);
        fromLayout.setOnClickListener(v -> etFromDate.performClick());

        LinearLayout toLayout = view.findViewById(R.id.toLayout);

        toLayout.setOnClickListener(v -> {
            etToDate.performClick();
        });

        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        btnApplyFilters.setOnClickListener(v -> applyFilters());

        ImageView btnResetIcon = view.findViewById(R.id.btnResetIcon);
        btnResetIcon.setOnClickListener(v -> resetFilters());

        chipLast7Days = view.findViewById(R.id.chipLast7Days);
        chipLastMonth = view.findViewById(R.id.chipLastMonth);
        chipCompletedOnly = view.findViewById(R.id.chipCompletedOnly);
        chipCanceledOnly = view.findViewById(R.id.chipCanceledOnly);
        chipPanicOnly = view.findViewById(R.id.chipPanicOnly);
        chipAll = view.findViewById(R.id.chipAll);
        setChipListeners();

        ridesRecyclerView = view.findViewById(R.id.ridesRecyclerView);

        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        allRides = new ArrayList<>();
        rideHistoryData = new ArrayList<>();

        rideAdapter = new RideAdapter(allRides, ride -> {

            AdminRideDetailsFragment fragment =
                    AdminRideDetailsFragment.newInstance(ride);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        ridesRecyclerView.setAdapter(rideAdapter);

        // Učitaj ride history iz API-ja
        loadRideHistory();

        return view;
    }

    private void loadRideHistory() {
        Log.d(TAG, "=== ADMIN RIDE HISTORY LOAD START ===");

        // Prikaži loading indicator
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        RideApi rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        Call<List<RideHistoryResponseDTO>> call = rideApi.getAdminRideHistory();

        Log.d(TAG, "API Call created for admin ride history");

        call.enqueue(new Callback<List<RideHistoryResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RideHistoryResponseDTO>> call, Response<List<RideHistoryResponseDTO>> response) {
                // Sakrij loading indicator
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }

                Log.d(TAG, "=== ADMIN RIDE HISTORY API RESPONSE ===");
                Log.d(TAG, "Status code: " + response.code());
                Log.d(TAG, "Response URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    rideHistoryData = response.body();
                    Log.d(TAG, "Loaded " + rideHistoryData.size() + " rides from API");

                    for (RideHistoryResponseDTO ride : rideHistoryData) {
                        Log.d(TAG, "Ride: " + ride.getStartLocation() + " -> " + ride.getEndLocation() + " | Status: " + ride.getStatus() + " | Panic: " + ride.getPanicSent());
                    }

                    // Konvertuj RideHistoryResponseDTO u Ride model
                    convertToRideModel(rideHistoryData);
                    rideAdapter.setRides(allRides);
                    Toast.makeText(getContext(), "Loaded " + allRides.size() + " rides", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                    Log.e(TAG, "Response message: " + response.message());
                    Toast.makeText(getContext(), "Failed to load ride history: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RideHistoryResponseDTO>> call, Throwable t) {
                // Sakrij loading indicator
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }

                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Log.e(TAG, "Request URL: " + call.request().url());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertToRideModel(List<RideHistoryResponseDTO> rideHistoryList) {
        allRides.clear();

        for (RideHistoryResponseDTO dto : rideHistoryList) {
            String price    = dto.getPrice() != null ? String.format("%.0f RSD", dto.getPrice()) : "N/A";
            String status   = dto.getStatus();
            String dateTime = formatDateTime(dto.getStartTime());

            Ride ride = new Ride(dto.getId(), dto.getRouteId(),
                    dto.getStartLocation(), dto.getEndLocation(),
                    price, status, dateTime, dto.getPanicSent());

            // Driver
            ride.setDriverFirstName(dto.getDriverFirstName());
            ride.setDriverLastName(dto.getDriverLastName());
            ride.setDriverPhoneNumber(dto.getDriverPhoneNumber());
            ride.setDriverProfilePictureUrl(dto.getDriverProfilePictureUrl());
            // Passenger
            ride.setPassengerFirstName(dto.getPassengerFirstName());
            ride.setPassengerLastName(dto.getPassengerLastName());
            ride.setPassengerPhoneNumber(dto.getPassengerPhoneNumber());
            ride.setPassengerProfilePictureUrl(dto.getPassengerProfilePictureUrl());
            ride.setPassengerEmail(dto.getPassengerEmail());
            // Times & details
            ride.setStartTime(dto.getStartTime());
            ride.setEstimatedEndTime(dto.getEstimatedEndTime());
            ride.setDistance(dto.getDistance());
            ride.setDuration(dto.getDuration());
            // Vehicle
            ride.setVehicleModel(dto.getVehicleModel());
            ride.setVehicleLicensePlate(dto.getVehicleLicensePlate());

            allRides.add(ride);
            Log.d(TAG, "Converted ride: " + dto.getStartLocation() + " -> " + dto.getEndLocation()
                    + " | " + status + " | Panic: " + dto.getPanicSent());
        }
    }

    private String formatDateTime(String startTime) {
        if (startTime == null || startTime.isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(startTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing error: " + e.getMessage());
            return startTime;
        }
    }

    private void setChipListeners() {
        View.OnClickListener chipClickListener = v -> {
            // Reset svi chipovi
            chipLast7Days.setSelected(false);
            chipLastMonth.setSelected(false);
            chipCompletedOnly.setSelected(false);
            chipCanceledOnly.setSelected(false);
            chipPanicOnly.setSelected(false);
            chipAll.setSelected(false);

            // Označi trenutni kao selected
            ((Chip) v).setSelected(true);

            Calendar calendar = Calendar.getInstance();
            Date from = null;
            Date to = calendar.getTime();

            // Reset date filtere
            etFromDate.setText("");
            etToDate.setText("");

            // Primeni filter u zavisnosti od klika
            if (v == chipLast7Days) {
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                from = calendar.getTime();
                etFromDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(from));
                etToDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(to));
                applyFilters();
            } else if (v == chipLastMonth) {
                calendar.add(Calendar.MONTH, -1);
                from = calendar.getTime();
                etFromDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(from));
                etToDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(to));
                applyFilters();
            } else if (v == chipCompletedOnly) {
                filterByStatus("Finished");
            } else if (v == chipCanceledOnly) {
                filterByStatus("Canceled");
            } else if (v == chipPanicOnly) {
                // Filter samo voznje sa panicSent == true
                filterByPanic();
            } else if (v == chipAll) {
                // Prikaži sve bez filtera
                rideAdapter.setRides(allRides);
            }
        };

        chipLast7Days.setOnClickListener(chipClickListener);
        chipLastMonth.setOnClickListener(chipClickListener);
        chipCompletedOnly.setOnClickListener(chipClickListener);
        chipCanceledOnly.setOnClickListener(chipClickListener);
        chipPanicOnly.setOnClickListener(chipClickListener);
        chipAll.setOnClickListener(chipClickListener);
    }

    private void resetFilters() {
        etFromDate.setText("");
        etToDate.setText("");
        rideAdapter.setRides(allRides);
        chipLast7Days.setSelected(false);
        chipLastMonth.setSelected(false);
        chipCompletedOnly.setSelected(false);
        chipCanceledOnly.setSelected(false);
        chipPanicOnly.setSelected(false);
        chipAll.setSelected(false);
    }


    private void filterByPanic() {
        List<Ride> panicRides = new ArrayList<>();
        for (Ride ride : allRides) {
            if (ride.getPanicSent() != null && ride.getPanicSent()) {
                panicRides.add(ride);
            }
        }
        rideAdapter.setRides(panicRides);
    }

    private void filterByStatus(String status) {
        List<Ride> statusRides = new ArrayList<>();
        for (Ride ride : allRides) {
            if (ride.getStatus().equalsIgnoreCase(status)) {
                statusRides.add(ride);
            }
        }
        rideAdapter.setRides(statusRides);
    }

    private void applyFilters() {
        String fromDateStr = etFromDate.getText().toString().trim();
        String toDateStr = etToDate.getText().toString().trim();

        Date fromDate = fromDateStr.isEmpty() ? null : parsePickerDate(fromDateStr);
        Date toDate = toDateStr.isEmpty() ? null : parsePickerDate(toDateStr);

        List<Ride> filteredRides = new ArrayList<>();

        for (Ride ride : allRides) {
            boolean match = true;

            String fromDateFlag = "PASS";
            String toDateFlag = "PASS";

            Date rideStartDate = parseRideDate(ride.getDateTime());
            if (rideStartDate != null) {
                if (fromDate != null && rideStartDate.before(fromDate)) {
                    match = false;
                    fromDateFlag = "FAIL";
                }
                if (toDate != null && rideStartDate.after(toDate)) {
                    match = false;
                    toDateFlag = "FAIL";
                }
            } else {
                match = false;
                fromDateFlag = "NULL";
                toDateFlag = "NULL";
            }

            System.out.println("Ride: " + ride.getFrom() + " → " + ride.getTo());
            System.out.println("From flag: " + fromDateFlag + ", To flag: " + toDateFlag);
            System.out.println("-----");

            if (match) {
                filteredRides.add(ride);
            }
        }

        rideAdapter.setRides(filteredRides);
    }



    private void setupDatePickers() {
        etFromDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {

                        String dayMonth = String.format(
                                Locale.getDefault(),
                                "%02d.%02d.",
                                dayOfMonth,
                                month + 1
                        );

                        String formattedDate = dayMonth + "\n" + year;
                        etFromDate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });

        etToDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {

                        String dayMonth = String.format(
                                Locale.getDefault(),
                                "%02d.%02d.",
                                dayOfMonth,
                                month + 1
                        );

                        String formattedDate = dayMonth + "\n" + year;
                        etToDate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });
    }

    private Date parseRideDate(String rideDateStr) {
        if (rideDateStr == null || rideDateStr.isEmpty()) return null;

        try {
            String startDateStr = rideDateStr.split(",")[0];
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            return sdf.parse(startDateStr);
        } catch (ParseException e) {
            Log.e("DateParsing", "Failed to parse ride date: " + rideDateStr, e);
            return null;
        }
    }

    private Date parsePickerDate(String pickerDateStr) {
        if (pickerDateStr == null || pickerDateStr.isEmpty()) return null;

        try {
            String clean = pickerDateStr.replace("\n", "");
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.parse(clean);
        } catch (ParseException e) {
            Log.e("DateParsing", "Failed to parse picker date: " + pickerDateStr, e);
            return null;
        }
    }

}


