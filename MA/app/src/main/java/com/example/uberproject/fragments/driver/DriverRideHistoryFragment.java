package com.example.uberproject.fragments.driver;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.adapters.RideAdapter;
import com.example.uberproject.models.Ride;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DriverRideHistoryFragment extends Fragment {

    private AutoCompleteTextView etFromDate, etToDate, etStatus;
    private AppCompatButton btnApplyFilters;
    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> allRides;
    private Chip chipLast7Days, chipLastMonth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_ride_history, container, false);

        etFromDate = view.findViewById(R.id.etFromDate);
        etToDate = view.findViewById(R.id.etToDate);
        setupDatePickers();
        etStatus = view.findViewById(R.id.etStatus);
        setupStatusDropdown();

        LinearLayout fromLayout = view.findViewById(R.id.fromLayout);
        fromLayout.setOnClickListener(v -> etFromDate.performClick());

        LinearLayout toLayout = view.findViewById(R.id.toLayout);

        toLayout.setOnClickListener(v -> {
            etToDate.performClick(); // ovo otvara dropdown ili date picker
        });

        btnApplyFilters = view.findViewById(R.id.btnApplyFilters); // dodaj ID u XML
        btnApplyFilters.setOnClickListener(v -> applyFilters());

        ImageView btnResetIcon = view.findViewById(R.id.btnResetIcon);
        btnResetIcon.setOnClickListener(v -> resetFilters());

        chipLast7Days = view.findViewById(R.id.chipLast7Days);
        chipLastMonth = view.findViewById(R.id.chipLastMonth);
        setChipListeners();

        ridesRecyclerView = view.findViewById(R.id.ridesRecyclerView);

        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Mock data
        allRides = new ArrayList<>();
        allRides.add(new Ride(1, "Stražilovska 32", "Bulevar Kralja Petra 7", "1,050 RSD", "Completed", "14 Mar 2025, 15:32 - 16:05"));
        allRides.add(new Ride(2, "Novi Sad", "Beograd", "1,250 RSD", "Completed", "14 Mar 2025, 14:32 - 15:05"));
        allRides.add(new Ride(3, "Zemun", "Novi Beograd", "1,670 RSD", "Completed", "14 Mar 2025, 12:32 - 13:05"));

        rideAdapter = new RideAdapter(allRides, ride -> {

            DriverRideDetailsFragment fragment =
                    DriverRideDetailsFragment.newInstance(ride);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        ridesRecyclerView.setAdapter(rideAdapter);

        return view;
    }

    private void setChipListeners() {
        View.OnClickListener chipClickListener = v -> {
            chipLast7Days.setSelected(false);
            chipLastMonth.setSelected(false);

            v.setSelected(true); // samo kliknuti chip postaje aktivan

            Calendar calendar = Calendar.getInstance();
            Date from = null;
            Date to = calendar.getTime();

            if (v == chipLast7Days) {
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                from = calendar.getTime();
            } else if (v == chipLastMonth) {
                calendar.add(Calendar.MONTH, -1);
                from = calendar.getTime();
            }

            // Postavi tekst u dropdown polja i primeni filter
            if (from != null) {
                etFromDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(from));
            }
            etToDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(to));

            applyFilters();
        };

        chipLast7Days.setOnClickListener(chipClickListener);
        chipLastMonth.setOnClickListener(chipClickListener);
    }

    private void resetFilters() {
        etFromDate.setText("");
        etToDate.setText("");
        etStatus.setText("");
        rideAdapter.setRides(allRides);
        chipLast7Days.setSelected(false);
        chipLastMonth.setSelected(false);
    }


    private void applyFilters() {
        String fromDateStr = etFromDate.getText().toString().trim();
        String toDateStr = etToDate.getText().toString().trim();
        String statusStr = etStatus.getText().toString().trim();

        Date fromDate = fromDateStr.isEmpty() ? null : parsePickerDate(fromDateStr);
        Date toDate = toDateStr.isEmpty() ? null : parsePickerDate(toDateStr);

        List<Ride> filteredRides = new ArrayList<>();

        for (Ride ride : allRides) {
            boolean match = true;

            // Flagovi za debug
            String statusFlag = "PASS";
            String fromDateFlag = "PASS";
            String toDateFlag = "PASS";

            // Filter po statusu
            if (!statusStr.isEmpty() && !ride.getStatus().equalsIgnoreCase(statusStr)) {
                match = false;
                statusFlag = "FAIL";
            }

            // Filter po datumu
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
                // Ako je rideStartDate null
                match = false;
                fromDateFlag = "NULL";
                toDateFlag = "NULL";
            }

            // Log za debug
            System.out.println("Ride: " + ride.getFrom() + " → " + ride.getTo());
            System.out.println("Status flag: " + statusFlag + ", From flag: " + fromDateFlag + ", To flag: " + toDateFlag);
            System.out.println("-----");

            if (match) {
                filteredRides.add(ride);
            }
        }

        rideAdapter.setRides(filteredRides);
    }


    private void setupStatusDropdown() {
        String[] statusOptions = {"Completed", "Canceled"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );

        etStatus.setAdapter(adapter);

        etStatus.setOnClickListener(v -> etStatus.showDropDown());

        etStatus.setKeyListener(null);
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
