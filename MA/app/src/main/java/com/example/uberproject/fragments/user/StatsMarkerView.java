package com.example.uberproject.fragments.user;

import android.content.Context;
import android.widget.TextView;

import com.example.uberproject.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.Locale;

public class StatsMarkerView extends MarkerView {

    private final TextView tvValue;
    private final String dataType;

    public StatsMarkerView(Context context, String dataType) {
        super(context, R.layout.marker_stats);
        this.dataType = dataType;
        tvValue = findViewById(R.id.tvMarkerValue);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (tvValue != null) {
            float val = e.getY();
            String formatted;
            switch (dataType) {
                case "distance":
                    formatted = String.format(Locale.getDefault(), "%.1f km", val);
                    break;
                case "money":
                    formatted = String.format(Locale.getDefault(), "%,.0f RSD", val);
                    break;
                default:
                    formatted = String.format(Locale.getDefault(), "%.0f rides", val);
                    break;
            }
            tvValue.setText(formatted);
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 10f);
    }
}
