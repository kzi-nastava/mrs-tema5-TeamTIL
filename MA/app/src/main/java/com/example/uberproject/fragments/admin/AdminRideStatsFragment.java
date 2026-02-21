package com.example.uberproject.fragments.admin;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.response.RideStatsDayDTO;
import com.example.uberproject.dto.response.RideStatsResponseDTO;
import com.example.uberproject.fragments.user.StatsMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRideStatsFragment extends Fragment {

    private TextView tvFromDate, tvToDate;
    private TextView tvTotalRides, tvAvgRides;
    private TextView tvTotalDistance, tvAvgDistance;
    private TextView tvTotalMoney, tvAvgMoney, tvMoneyLabel, tvSpendingChartLabel;
    private TextView tvRidesCumulative, tvDistanceCumulative, tvMoneyCumulative;
    private Button btnLast7Days, btnLast30Days, btnLast3Months;
    private Button btnViewDrivers, btnViewPassengers;
    private EditText etFilterEmail;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private LineChart chartRidesPerDay, chartDistancePerDay, chartMoneyPerDay;

    private String selectedFromDate = null;
    private String selectedToDate = null;
    private String currentRole = "DRIVER"; // default
    private String filterEmail = null;

    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private static final SimpleDateFormat API_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_ride_stats, container, false);
        bindViews(view);
        setupViewTypeToggle();
        setupDatePickers();
        setupQuickFilters();
        setupEmailFilter();
        applyLast30Days();
        return view;
    }

    private void bindViews(View view) {
        tvFromDate = view.findViewById(R.id.tvFromDate);
        tvToDate = view.findViewById(R.id.tvToDate);
        tvTotalRides = view.findViewById(R.id.tvTotalRides);
        tvAvgRides = view.findViewById(R.id.tvAvgRides);
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance);
        tvAvgDistance = view.findViewById(R.id.tvAvgDistance);
        tvTotalMoney = view.findViewById(R.id.tvTotalMoney);
        tvAvgMoney = view.findViewById(R.id.tvAvgMoney);
        tvMoneyLabel = view.findViewById(R.id.tvMoneyLabel);
        tvSpendingChartLabel = view.findViewById(R.id.tvSpendingChartLabel);
        tvRidesCumulative = view.findViewById(R.id.tvRidesCumulative);
        tvDistanceCumulative = view.findViewById(R.id.tvDistanceCumulative);
        tvMoneyCumulative = view.findViewById(R.id.tvMoneyCumulative);
        btnLast7Days = view.findViewById(R.id.btnLast7Days);
        btnLast30Days = view.findViewById(R.id.btnLast30Days);
        btnLast3Months = view.findViewById(R.id.btnLast3Months);
        btnViewDrivers = view.findViewById(R.id.btnViewDrivers);
        btnViewPassengers = view.findViewById(R.id.btnViewPassengers);
        etFilterEmail = view.findViewById(R.id.etFilterEmail);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        chartRidesPerDay = view.findViewById(R.id.chartRidesPerDay);
        chartDistancePerDay = view.findViewById(R.id.chartDistancePerDay);
        chartMoneyPerDay = view.findViewById(R.id.chartMoneyPerDay);
    }

    private void setupViewTypeToggle() {
        updateToggleUI(); // initial state: Drivers active

        btnViewDrivers.setOnClickListener(v -> {
            currentRole = "DRIVER";
            updateToggleUI();
            updateMoneyLabels();
            loadStats();
        });

        btnViewPassengers.setOnClickListener(v -> {
            currentRole = "PASSENGER";
            updateToggleUI();
            updateMoneyLabels();
            loadStats();
        });
    }

    private void updateToggleUI() {
        boolean isDrivers = "DRIVER".equals(currentRole);
        btnViewDrivers.setBackgroundColor(isDrivers ? Color.parseColor("#F5C518") : Color.parseColor("#1E1E35"));
        btnViewDrivers.setTextColor(isDrivers ? Color.BLACK : Color.parseColor("#AAAACC"));
        btnViewPassengers.setBackgroundColor(!isDrivers ? Color.parseColor("#F5C518") : Color.parseColor("#1E1E35"));
        btnViewPassengers.setTextColor(!isDrivers ? Color.BLACK : Color.parseColor("#AAAACC"));
    }

    private void updateMoneyLabels() {
        boolean isDrivers = "DRIVER".equals(currentRole);
        if (tvMoneyLabel != null) tvMoneyLabel.setText(isDrivers ? "TOTAL REVENUE" : "TOTAL SPENT");
        if (tvSpendingChartLabel != null)
            tvSpendingChartLabel.setText(isDrivers ? "Revenue per Day (RSD)" : "Spending per Day (RSD)");
    }

    private void setupEmailFilter() {
        etFilterEmail.addTextChangedListener(new TextWatcher() {
            private Runnable debounce;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Debounce: wait 600ms after typing stops before loading
                if (debounce != null) etFilterEmail.removeCallbacks(debounce);
                debounce = () -> {
                    String input = s.toString().trim();
                    filterEmail = input.isEmpty() ? null : input;
                    loadStats();
                };
                etFilterEmail.postDelayed(debounce, 600);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupDatePickers() {
        tvFromDate.setOnClickListener(v -> showDatePicker(true));
        tvToDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            String display = DISPLAY_FORMAT.format(selected.getTime());
            if (isFrom) {
                selected.set(Calendar.HOUR_OF_DAY, 0); selected.set(Calendar.MINUTE, 0); selected.set(Calendar.SECOND, 0);
                selectedFromDate = API_FORMAT.format(selected.getTime());
                tvFromDate.setText(display);
            } else {
                selected.set(Calendar.HOUR_OF_DAY, 23); selected.set(Calendar.MINUTE, 59); selected.set(Calendar.SECOND, 59);
                selectedToDate = API_FORMAT.format(selected.getTime());
                tvToDate.setText(display);
            }
            loadStats();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupQuickFilters() {
        btnLast7Days.setOnClickListener(v -> { applyQuickFilter(-7); highlightButton(btnLast7Days); });
        btnLast30Days.setOnClickListener(v -> { applyLast30Days(); highlightButton(btnLast30Days); });
        btnLast3Months.setOnClickListener(v -> { applyQuickFilter(-90); highlightButton(btnLast3Months); });
    }

    private void highlightButton(Button active) {
        int inactive = Color.parseColor("#1E1E35");
        int activeColor = Color.parseColor("#F5C518");
        btnLast7Days.setBackgroundColor(inactive);
        btnLast30Days.setBackgroundColor(inactive);
        btnLast3Months.setBackgroundColor(inactive);
        active.setBackgroundColor(activeColor);
    }

    private void applyLast30Days() {
        applyQuickFilter(-30);
        highlightButton(btnLast30Days);
    }

    private void applyQuickFilter(int daysOffset) {
        Calendar to = Calendar.getInstance();
        to.set(Calendar.HOUR_OF_DAY, 23); to.set(Calendar.MINUTE, 59); to.set(Calendar.SECOND, 59);
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DAY_OF_MONTH, daysOffset);
        from.set(Calendar.HOUR_OF_DAY, 0); from.set(Calendar.MINUTE, 0); from.set(Calendar.SECOND, 0);
        selectedFromDate = API_FORMAT.format(from.getTime());
        selectedToDate = API_FORMAT.format(to.getTime());
        tvFromDate.setText(DISPLAY_FORMAT.format(from.getTime()));
        tvToDate.setText(DISPLAY_FORMAT.format(to.getTime()));
        loadStats();
    }

    private void loadStats() {
        showLoading(true);
        RideApi rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        rideApi.getAdminStats(currentRole, filterEmail, selectedFromDate, selectedToDate)
                .enqueue(new Callback<RideStatsResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<RideStatsResponseDTO> call,
                                   @NonNull Response<RideStatsResponseDTO> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindStats(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to load statistics", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<RideStatsResponseDTO> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindStats(RideStatsResponseDTO stats) {
        tvTotalRides.setText(String.valueOf(stats.getTotalRides()));
        tvAvgRides.setText(String.format(Locale.getDefault(), "Avg %.1f / day", stats.getAvgRidesPerDay()));
        tvTotalDistance.setText(String.format(Locale.getDefault(), "%.0f", stats.getTotalDistanceKm()));
        tvAvgDistance.setText(String.format(Locale.getDefault(), "Avg %.1f km / day", stats.getAvgDistancePerDay()));
        tvTotalMoney.setText(String.format(Locale.getDefault(), "%,.0f", stats.getTotalMoney()));
        tvAvgMoney.setText(String.format(Locale.getDefault(), "Avg %,.0f RSD / day", stats.getAvgMoneyPerDay()));
        tvRidesCumulative.setText(String.valueOf(stats.getTotalRides()));
        tvDistanceCumulative.setText(String.format(Locale.getDefault(), "%.1f km", stats.getTotalDistanceKm()));
        tvMoneyCumulative.setText(String.format(Locale.getDefault(), "%,.0f RSD", stats.getTotalMoney()));

        tvEmpty.setVisibility(stats.getTotalRides() == 0 ? View.VISIBLE : View.GONE);

        List<RideStatsDayDTO> days = stats.getDays();
        setupChart(chartRidesPerDay, days, "rides", Color.parseColor("#F5C518"));
        setupChart(chartDistancePerDay, days, "distance", Color.parseColor("#9B8FFF"));
        setupChart(chartMoneyPerDay, days, "money", Color.parseColor("#1DE9B6"));
    }

    private void setupChart(LineChart chart, List<RideStatsDayDTO> days, String dataType, int color) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            RideStatsDayDTO day = days.get(i);
            float value;
            switch (dataType) {
                case "distance": value = (float) day.getDistanceKm(); break;
                case "money":    value = (float) day.getMoneyAmount(); break;
                default:         value = day.getRidesCount(); break;
            }
            entries.add(new Entry(i, value));
            String date = day.getDate();
            if (date != null && date.length() >= 10) labels.add(date.substring(5, 10).replace("-", "/"));
            else labels.add(date != null ? date : "");
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(25);
        dataSet.setFillColor(color);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(color);

        chart.setData(new LineData(dataSet));
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setExtraBottomOffset(8f);

        StatsMarkerView marker = new StatsMarkerView(requireContext(), dataType);
        chart.setMarker(marker);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#666688"));
        xAxis.setTextSize(9f);
        xAxis.setGridColor(Color.parseColor("#1E1E35"));
        xAxis.setAxisLineColor(Color.parseColor("#1E1E35"));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(6, labels.size()), true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < labels.size()) ? labels.get(idx) : "";
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#666688"));
        leftAxis.setTextSize(9f);
        leftAxis.setGridColor(Color.parseColor("#1E1E35"));
        leftAxis.setAxisLineColor(Color.parseColor("#1E1E35"));
        leftAxis.setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.animateX(500);
        chart.invalidate();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
