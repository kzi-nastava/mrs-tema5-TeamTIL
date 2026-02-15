package com.example.uberproject.fragments.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.api.PublicApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.response.VehicleStatusResponseDTO;

import org.osmdroid.config.Configuration;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        webView = view.findViewById(R.id.webViewMap);

        File cachePath = new File(requireContext().getCacheDir(), "osmdroid");
        Configuration.getInstance().setOsmdroidBasePath(cachePath);
        Configuration.getInstance().setOsmdroidTileCache(cachePath);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.loadUrl("file:///android_asset/map.html");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                loadActiveVehicles();
            }
        });

        return view;
    }

    public void loadActiveVehicles(){
        PublicApi publicApi = RetrofitClient.getInstance(requireContext()).create(PublicApi.class);
        Call<List<VehicleStatusResponseDTO>> call = publicApi.getActiveVehicles();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<VehicleStatusResponseDTO>> call, Response<List<VehicleStatusResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<VehicleStatusResponseDTO> vehicles = response.body();

                    clearMarkers();

                    for (VehicleStatusResponseDTO v : vehicles) {

                        if (v.getLatitude() != null &&
                                v.getLongitude() != null) {
                            addMarker(
                                    v.getLatitude(),
                                    v.getLongitude(),
                                    v.getName() + " (" + v.getLicensePlate() + ")",
                                    v.getAvailable()
                            );
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load active vehicles: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VehicleStatusResponseDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearMarkers() {
        if (webView != null) {
            webView.post(() ->
                    webView.evaluateJavascript("clearMarkers()", null)
            );
        }
    }

    public void addMarker(double lat, double lng, String title, boolean available) {
        String safeTitle = title.replace("'", "\\'");
        String js = "addMarker(" + lat + "," + lng + ",'" + safeTitle + "'," + available + ")";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }

    public void drawRoute(List<double[]> coords, String estimatedTime){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0; i<coords.size(); i++){
            double[] c = coords.get(i);
            sb.append("[").append(c[0]).append(",").append(c[1]).append("]");
            if(i<coords.size()-1) sb.append(",");
        }
        sb.append("]");
        String js = "drawRoute(" + sb.toString() + ",'" + estimatedTime + "')";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }
}
