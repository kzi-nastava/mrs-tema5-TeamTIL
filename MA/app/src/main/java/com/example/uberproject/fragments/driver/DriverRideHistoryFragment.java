package com.example.uberproject.fragments.driver;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.uberproject.dto.response.DriverRideHistoryResponseDTO;
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

public class DriverRideHistoryFragment extends Fragment {

    private AutoCompleteTextView etFromDate, etToDate;
    private AppCompatButton btnApplyFilters;
    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> allRides;

    private List<DriverRideHistoryResponseDTO> rideHistoryData;
    private Chip chipLast7Days, chipLastMonth, chipCompletedOnly, chipCanceledOnly, chipAll;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_ride_history, container, false);

        etFromDate = view.findViewById(R.id.etFromDate);
        etToDate = view.findViewById(R.id.etToDate);
        setupDatePickers();

        LinearLayout fromLayout = view.findViewById(R.id.fromLayout);
        fromLayout.setOnClickListener(v -> etFromDate.performClick());

        LinearLayout toLayout = view.findViewById(R.id.toLayout);

        toLayout.setOnClickListener(v -> etToDate.performClick());

        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        btnApplyFilters.setOnClickListener(v -> applyFilters());

        ImageView btnResetIcon = view.findViewById(R.id.btnResetIcon);
        btnResetIcon.setOnClickListener(v -> resetFilters());

        chipLast7Days = view.findViewById(R.id.chipLast7Days);
        chipLastMonth = view.findViewById(R.id.chipLastMonth);
        chipCompletedOnly = view.findViewById(R.id.chipCompletedOnly);
        chipCanceledOnly = view.findViewById(R.id.chipCanceledOnly);
        chipAll = view.findViewById(R.id.chipAll);
        setChipListeners();

        ridesRecyclerView = view.findViewById(R.id.ridesRecyclerView);

        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        allRides = new ArrayList<>();
        rideHistoryData = new ArrayList<>();

        rideAdapter = new RideAdapter(allRides, ride -> {

            DriverRideHistoryResponseDTO selectedDto = null;

            for (DriverRideHistoryResponseDTO dto : rideHistoryData) {
                if (dto.getId().equals(ride.getId())) {
                    selectedDto = dto;
                    break;
                }
            }

            if (selectedDto == null) {
                Toast.makeText(getContext(), "Ride details not found", Toast.LENGTH_SHORT).show();
                return;
            }

            DriverRideDetailsFragment fragment =
                    DriverRideDetailsFragment.newInstance(selectedDto);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        ridesRecyclerView.setAdapter(rideAdapter);

        loadRideHistory();

        return view;
    }

    private void loadRideHistory() {
        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        String userEmail = tokenManager.getUserEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        RideApi rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        Call<List<DriverRideHistoryResponseDTO>> call = rideApi.getDriverRideHistory(userEmail);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<DriverRideHistoryResponseDTO>> call, Response<List<DriverRideHistoryResponseDTO>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    rideHistoryData = response.body();
                    convertToRideModel(rideHistoryData);
                    rideAdapter.setRides(allRides);
                    Toast.makeText(getContext(), "Loaded " + allRides.size() + " rides", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to load ride history: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DriverRideHistoryResponseDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertToRideModel(List<DriverRideHistoryResponseDTO> rideHistoryList) {
        allRides.clear();

        for (DriverRideHistoryResponseDTO rideDto : rideHistoryList) {
            String from = rideDto.getFrom();
            String to = rideDto.getTo();
            String price = rideDto.getPrice() != null ? rideDto.getPrice() : "N/A";
            String status = rideDto.getStatus();
            String dateTime = rideDto.getDate() + ", " + rideDto.getStartTime();
            Boolean panicSent = rideDto.getPanicSent();

            Ride ride = new Ride(rideDto.getId(), from, to, price, status, dateTime, panicSent);
            allRides.add(ride);
        }
    }

    private void setChipListeners() {
        View.OnClickListener chipClickListener = v -> {
            chipLast7Days.setSelected(false);
            chipLastMonth.setSelected(false);
            chipCompletedOnly.setSelected(false);
            chipCanceledOnly.setSelected(false);
            chipAll.setSelected(false);

            v.setSelected(true); // samo kliknuti chip postaje aktivan

            Calendar calendar = Calendar.getInstance();
            Date from;
            Date to = calendar.getTime();

            etFromDate.setText("");
            etToDate.setText("");

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
            } else if (v == chipCompletedOnly || v == chipCanceledOnly) {
                applyFilters();
            } else if (v == chipAll) {
                rideAdapter.setRides(allRides);
            }
        };

        chipLast7Days.setOnClickListener(chipClickListener);
        chipLastMonth.setOnClickListener(chipClickListener);
        chipCompletedOnly.setOnClickListener(chipClickListener);
        chipCanceledOnly.setOnClickListener(chipClickListener);
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
        chipAll.setSelected(false);
    }


    private void applyFilters() {
        String fromDateStr = etFromDate.getText().toString().trim();
        String toDateStr = etToDate.getText().toString().trim();

        Date fromDate = fromDateStr.isEmpty() ? null : parsePickerDate(fromDateStr);
        Date toDate = toDateStr.isEmpty() ? null : parsePickerDate(toDateStr);

        List<Ride> filteredRides = new ArrayList<>();

        for (Ride ride : allRides) {
            boolean match = true;

            Date rideStartDate = parseRideDate(ride.getDateTime());
            if (rideStartDate != null) {
                if (fromDate != null && rideStartDate.before(fromDate)) {
                    match = false;
                }
                if (toDate != null && rideStartDate.after(toDate)) {
                    match = false;
                }
            } else {
                match = false;
            }

            String rideStatus = ride.getStatus();

            if (chipCompletedOnly.isSelected() && !"Completed".equalsIgnoreCase(rideStatus)) {
                match = false;
            }

            if (chipCanceledOnly.isSelected() && !"Canceled".equalsIgnoreCase(rideStatus)) {
                match = false;
            }

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
