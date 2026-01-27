package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Route;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RouteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    @Autowired
    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

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
            JSONObject feature = json.getJSONArray("features").getJSONObject(0);

            // Izvuci distance i duration
            JSONObject segment = feature.getJSONObject("properties")
                    .getJSONArray("segments")
                    .getJSONObject(0);
            double distanceKm = segment.getDouble("distance") / 1000.0;
            double durationMin = segment.getDouble("duration") / 60.0;

            // Izvuci koordinate putanje
            JSONArray coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates");
            List<List<Double>> routeCoordinates = new ArrayList<>();
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray coord = coordinates.getJSONArray(i);
                List<Double> point = List.of(coord.getDouble(0), coord.getDouble(1));
                routeCoordinates.add(point);
            }

            return new RouteEstimation(distanceKm, durationMin, routeCoordinates);
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
        public final List<List<Double>> routeCoordinates; // [[lon, lat], [lon, lat], ...]

        public RouteEstimation(double distanceKm, double durationMin, List<List<Double>> routeCoordinates) {
            this.distanceKm = distanceKm;
            this.durationMin = durationMin;
            this.routeCoordinates = routeCoordinates;
        }
    }

    public Route save(Route route) {
        return routeRepository.save(route);
    }
}
