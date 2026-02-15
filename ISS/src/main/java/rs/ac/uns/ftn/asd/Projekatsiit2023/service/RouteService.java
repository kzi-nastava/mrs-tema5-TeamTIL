package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.FavoriteRouteDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.AddToFavoritesResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    @Value("${ors.api.key}")
    private String orsApiKey;

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    @Autowired
    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }
    @Autowired
    private RegisteredUserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public RouteEstimation estimateRoute(double startLat, double startLon, double endLat, double endLon) {
        // ORS očekuje format: lon,lat (ne lat,lon!)
        String url = String.format(Locale.US,
                "https://api.openrouteservice.org/v2/directions/driving-car?api_key=%s&start=%.6f,%.6f&end=%.6f,%.6f",
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
            logger.error("Error calling ORS API: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing ORS response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * @param routeCoordinates [[lon, lat], [lon, lat], ...]
     */
    public record RouteEstimation(double distanceKm, double durationMin, List<List<Double>> routeCoordinates) {
    }

    public Route save(Route route) {
        return routeRepository.save(route);
    }

    public List<double[]> getRouteFromOSRM(double startLat, double startLng, double endLat, double endLng) {
        try {
            String url = String.format(
                    "http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    startLng, startLat, endLng, endLat
            );

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            List<double[]> coordinatesList = new ArrayList<>();

            JsonNode coords = root.path("routes").get(0).path("geometry").path("coordinates");
            for (JsonNode point : coords) {
                double lng = point.get(0).asDouble();
                double lat = point.get(1).asDouble();
                coordinatesList.add(new double[]{lat, lng});
            }

            return coordinatesList;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Transactional
    public AddToFavoritesResponseDTO addToFavorites(Integer routeId, String userEmail) {
        RegisteredUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (user.getFavoriteRoutes().contains(route)) {
            return new AddToFavoritesResponseDTO(
                    routeId,
                    "Route is already in favorites",
                    true
            );
        }

        user.getFavoriteRoutes().add(route);
        userRepository.save(user);

        return new AddToFavoritesResponseDTO(
                routeId,
                "Route added to favorites",
                true
        );
    }

    @Transactional
    public AddToFavoritesResponseDTO removeFromFavorites(Integer routeId, String userEmail) {
        RegisteredUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        user.getFavoriteRoutes().remove(route);
        userRepository.save(user);

        return new AddToFavoritesResponseDTO(
                routeId,
                "Route removed from favorites",
                false
        );
    }

    public List<FavoriteRouteDTO> getFavoriteRoutes(String userEmail) {
        RegisteredUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getFavoriteRoutes().stream()
                .map(route -> {
                    List<Location> locations = route.getLocations();

                    if (locations == null || locations.isEmpty()) {
                        return null;
                    }

                    String startLocation = locations.get(0).getAddress();
                    String endLocation = locations.get(locations.size() - 1).getAddress();

                    List<String> intermediateStops = locations.subList(1, locations.size() - 1)
                            .stream()
                            .map(Location::getAddress)
                            .toList();

                    return new FavoriteRouteDTO(
                            route.getId(),
                            startLocation,
                            endLocation,
                            intermediateStops,
                            route.getDistance(),
                            route.getEstimatedTime() / 60.0
                    );
                })
                .filter(dto -> dto != null)
                .toList();
    }

    public boolean isRouteFavorite(Integer routeId, String userEmail) {
        RegisteredUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getFavoriteRoutes().stream()
                .anyMatch(route -> route.getId().equals(routeId));
    }
}
