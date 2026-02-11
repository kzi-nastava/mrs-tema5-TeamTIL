package rs.ac.uns.ftn.asd.Projekatsiit2023.service.ride_simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.WebSocketRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Route;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RideSimulation {
    private final int rideId;
    private final List<double[]> routeCoordinates;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private int currentSegment;
    private int currentStep;
    private double totalPriceCache;

    private boolean simulationStarted = false;
    private double currentPrice;
    private int remainingDuration;
    private double[] currentPosition;
    @Autowired
    private RideService rideService;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private RouteService routeService;

    public RideSimulation(int rideId, RideService rideService, RideRepository rideRepository, RouteService routeService) {
        this.rideId = rideId;
        this.rideService = rideService;
        this.rideRepository = rideRepository;
        this.routeService = routeService;

        this.routeCoordinates = rideService.getRouteForRide(rideId);

        Ride ride = rideRepository.findByIdWithDriverAndVehicle(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id " + rideId));

        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                ride.getStartLocation().getLatitude(), ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getLatitude(), ride.getEndLocation().getLongitude());

        double basePrice = estimation.distanceKm() * 120; // TODO: change based on price config
        double multiplier = switch (ride.getDriver().getVehicle().getType() == null ? "STANDARD" : ride.getDriver().getVehicle().getType().toString().toUpperCase()) {
            case "LUXURY" -> 1.5;
            case "VAN" -> 1.3;
            default -> 1.0;
        };

        this.totalPriceCache = Math.round(basePrice * multiplier * 100.0) / 100.0;

        this.currentSegment = 0;
        this.currentStep = 0;
        this.currentPosition = routeCoordinates.get(0);
        this.remainingDuration = (int)Math.round(estimation.durationMin());
        this.currentPrice = 0;
    }

    public void addSession(WebSocketSession session) {
        sessions.add(session);

        if (currentPosition != null) {
            sendToSession(session, currentPosition[0], currentPosition[1], remainingDuration, currentPrice);
        }

        if (!simulationStarted) {
            simulationStarted = true;
            startSimulation(remainingDuration, totalPriceCache);
        }
    }

    private void sendToSession(WebSocketSession session, double lat, double lng, int remainingDuration, double price) {
        try {
            WebSocketRideDTO dto = new WebSocketRideDTO(lat, lng, remainingDuration, (int)Math.round(price));
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(dto)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void simulationStep(double totalETA, double totalPrice){
        double[] start = routeCoordinates.get(currentSegment);
        double[] end = routeCoordinates.get(currentSegment + 1);
        int steps = 20;

        double deltaLat = (end[0] - start[0]) / steps;
        double deltaLng = (end[1] - start[1]) / steps;

        double lat = start[0] + deltaLat * currentStep;
        double lng = start[1] + deltaLng * currentStep;

        remainingDuration = calculateRemainingDuration(currentSegment, currentStep, routeCoordinates.size() - 1, steps, totalETA);
        currentPrice = calculatePrice(currentSegment, currentStep, steps, routeCoordinates, totalPrice);
        currentPosition = new double[]{lat, lng};

        if (!sessions.isEmpty()) {
            sendToAll(new WebSocketRideDTO(lat, lng, remainingDuration, (int)Math.round(currentPrice)));
        }

        currentStep++;
        if (currentStep >= steps) {
            currentStep = 0;
            currentSegment++;
            if (currentSegment >= routeCoordinates.size() - 1) {
                executor.shutdown();
            }
        }
    }


    private void startSimulation(double totalETA, double totalPrice) {
        executor.scheduleAtFixedRate(() -> simulationStep(totalETA, totalPrice), 500, 200, TimeUnit.MILLISECONDS);
    }

    private void sendToAll(WebSocketRideDTO dto) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(dto)));
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public String getCurrentStateJson() {
        try {
            return new ObjectMapper().writeValueAsString(new WebSocketRideDTO(currentPosition[0], currentPosition[1], remainingDuration, (int)Math.round(currentPrice)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private double calculatePrice(int currentSegmentIndex, int currentStepInSegment, int stepsPerSegment, List<double[]> routeCoordinates, double totalPrice) {
        double totalDistance = 0;
        for (int i = 0; i < routeCoordinates.size() - 1; i++) {
            totalDistance += distanceInMeters(routeCoordinates.get(i), routeCoordinates.get(i + 1));
        }

        double distanceCovered = 0;
        for (int i = 0; i < currentSegmentIndex; i++) {
            distanceCovered += distanceInMeters(routeCoordinates.get(i), routeCoordinates.get(i + 1));
        }

        double[] start = routeCoordinates.get(currentSegmentIndex);
        double[] end = routeCoordinates.get(currentSegmentIndex + 1);
        double deltaLat = (end[0] - start[0]) / stepsPerSegment;
        double deltaLng = (end[1] - start[1]) / stepsPerSegment;

        double lat = start[0] + deltaLat * currentStepInSegment;
        double lng = start[1] + deltaLng * currentStepInSegment;
        distanceCovered += distanceInMeters(start, new double[]{lat, lng});

        return totalPrice * (distanceCovered / totalDistance);
    }

    private double distanceInMeters(double[] a, double[] b) {
        double R = 6371000;
        double lat1 = Math.toRadians(a[0]);
        double lat2 = Math.toRadians(b[0]);
        double dLat = Math.toRadians(b[0] - a[0]);
        double dLng = Math.toRadians(b[1] - a[1]);

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return R * c;
    }

    private int calculateRemainingDuration(int currentSegmentIndex, int currentStepInSegment, int totalSegments, int stepsPerSegment, double totalDurationMinutes) {
        int currentStep = currentSegmentIndex * stepsPerSegment + currentStepInSegment;
        int totalSteps = totalSegments * stepsPerSegment;

        double fractionLeft = 1.0 - ((double) currentStep / totalSteps);
        return Math.max(0, (int) Math.round(fractionLeft * totalDurationMinutes));
    }
}
