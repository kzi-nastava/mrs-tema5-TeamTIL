package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    @Value("${ors.api.key}")
    private String orsApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public RouteEstimation estimateRoute(double startLat, double startLon, double endLat, double endLon) {
        String url = String.format(
                "https://api.openrouteservice.org/v2/directions/driving-car?api_key=%s&start=%f,%f&end=%f,%f",
                orsApiKey, startLon, startLat, endLon, endLat
        );

        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            JSONArray segments = json.getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("properties")
                    .getJSONArray("segments");
            JSONObject segment = segments.getJSONObject(0);

            double distanceKm = segment.getDouble("distance") / 1000.0;
            double durationMin = segment.getDouble("duration") / 60.0;

            return new RouteEstimation(distanceKm, durationMin);
        } catch (RestClientException e) {
            logger.error("Error while sending request to ORS API: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error while processing ORS API response: {}", e.getMessage());
        }
        return null;
    }

    public static class RouteEstimation {
        public final double distanceKm;
        public final double durationMin;

        public RouteEstimation(double distanceKm, double durationMin) {
            this.distanceKm = distanceKm;
            this.durationMin = durationMin;
        }
    }
}
