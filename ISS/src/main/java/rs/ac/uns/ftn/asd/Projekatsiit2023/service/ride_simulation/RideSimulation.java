package rs.ac.uns.ftn.asd.Projekatsiit2023.service.ride_simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.WebSocketRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PriceConfig;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PriceConfigRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.VehicleRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class RideSimulation {
    private static final Logger logger = LoggerFactory.getLogger(RideSimulation.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int STEPS_PER_SEGMENT = 20;
    // Update vehicle position in DB every N simulation steps (~every 2s at 200ms/step)
    private static final int DB_UPDATE_INTERVAL = 10;

    private final int rideId;
    private final Integer vehicleId;
    private final List<double[]> routeCoordinates;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> simulationFuture;


    // Simulation state
    private int currentSegment = 0;
    private int currentStep = 0;
    private final AtomicInteger totalStepCounter = new AtomicInteger(0);

    private double totalPriceCache;
    private double totalEtaMinutes;

    private volatile boolean simulationStarted = false;
    private volatile boolean stopped = false;

    private volatile double currentPrice = 0;
    private volatile int remainingDuration;
    private volatile double[] currentPosition;

    private final VehicleRepository vehicleRepository;

    public RideSimulation(int rideId, RideService rideService, RideRepository rideRepository, RouteService routeService, PriceConfigRepository priceConfigRepository, VehicleRepository vehicleRepository) {
        this.rideId = rideId;
        this.vehicleRepository = vehicleRepository;

        Ride ride = rideRepository.findByIdWithDriverAndVehicle(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        this.vehicleId = ride.getDriver().getVehicle().getId();
        this.routeCoordinates = rideService.getRouteForRide(rideId);

        if (routeCoordinates.isEmpty()) {
            throw new RuntimeException("No route coordinates for ride: " + rideId);
        }

        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                ride.getStartLocation().getLatitude(), ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getLatitude(), ride.getEndLocation().getLongitude());

        PriceConfig priceConfig = priceConfigRepository.findByVehicleType(ride.getDriver().getVehicle().getType())
                .orElseThrow(() -> new RuntimeException("Price config not found"));

        this.totalEtaMinutes = estimation != null ? estimation.durationMin() : 30;
        this.totalPriceCache = priceConfig.getBasePrice()
                + (estimation != null ? estimation.distanceKm() : 0) * priceConfig.getPricePerKm();

        this.remainingDuration = (int) Math.round(totalEtaMinutes);
        this.currentPosition = routeCoordinates.get(0);
    }

    // Called by RideSimulationService when ride starts (maybe before any client connects).
    public synchronized void start() {
        if (simulationStarted || stopped) return;
        simulationStarted = true;
        simulationFuture = executor.scheduleAtFixedRate(
                this::simulationStep, 500, 200, TimeUnit.MILLISECONDS);
        logger.info("[Simulation] Started for rideId={}", rideId);
    }

    // Called when a WebSocket client connects to track this ride.
    public void addSession(WebSocketSession session) {
        sessions.add(session);
        // Immediately send current state to the newly connected client
        sendToSession(session, currentPosition[0], currentPosition[1], remainingDuration, currentPrice);
        // If ride hasn't started yet (edge case), start simulation
        start();
    }

    // Called externally (from RideService.endRide / stopRide) to terminate the simulation,
    // broadcast RIDE_ENDED to all connected clients, and release resources
    public void stopAndNotify() {
        if (stopped) return;
        stopped = true;
        if (simulationFuture != null) simulationFuture.cancel(false);
        executor.shutdown();
        broadcastRideEnded();
        logger.info("[Simulation] Stopped and clients notified for rideId={}", rideId);
    }

    public String getCurrentStateJson() {
        try {
            return MAPPER.writeValueAsString(
                    new WebSocketRideDTO(currentPosition[0], currentPosition[1],
                            remainingDuration, (int) Math.round(currentPrice)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public double getCurrentLatitude()  { return currentPosition[0]; }
    public double getCurrentLongitude() { return currentPosition[1]; }

    private void simulationStep() {
        if (stopped) return;
        if (currentSegment >= routeCoordinates.size() - 1) {
            // Reached destination naturally
            stopAndNotify();
            return;
        }

        double[] start = routeCoordinates.get(currentSegment);
        double[] end   = routeCoordinates.get(currentSegment + 1);

        double lat = start[0] + (end[0] - start[0]) / STEPS_PER_SEGMENT * currentStep;
        double lng = start[1] + (end[1] - start[1]) / STEPS_PER_SEGMENT * currentStep;

        remainingDuration = calculateRemainingDuration(currentSegment, currentStep,
                routeCoordinates.size() - 1, STEPS_PER_SEGMENT, totalEtaMinutes);
        currentPrice = calculatePrice(currentSegment, currentStep,
                STEPS_PER_SEGMENT, routeCoordinates, totalPriceCache);
        currentPosition = new double[]{lat, lng};

        // Broadcast to all connected clients
        if (!sessions.isEmpty()) {
            broadcastPosition(lat, lng);
        }

        // Periodically persist vehicle position to DB
        int step = totalStepCounter.incrementAndGet();
        if (step % DB_UPDATE_INTERVAL == 0) {
            persistVehiclePosition(lat, lng);
        }

        // Advance
        currentStep++;
        if (currentStep >= STEPS_PER_SEGMENT) {
            currentStep = 0;
            currentSegment++;
        }
    }

    private void broadcastPosition(double lat, double lng) {
        WebSocketRideDTO dto = new WebSocketRideDTO(lat, lng, remainingDuration, (int) Math.round(currentPrice));
        String json;
        try {
            json = MAPPER.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            return;
        }
        sessions.removeIf(s -> !s.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                logger.warn("[Simulation] Failed to send to session: {}", e.getMessage());
            }
        }
    }

    private void broadcastRideEnded() {
        String msg = "{\"type\":\"RIDE_ENDED\"}";
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(msg));
                    session.close();
                } catch (IOException e) {
                    logger.warn("[Simulation] Error closing session: {}", e.getMessage());
                }
            }
        }
        sessions.clear();
    }

    private void sendToSession(WebSocketSession session, double lat, double lng,
                               int duration, double price) {
        try {
            WebSocketRideDTO dto = new WebSocketRideDTO(lat, lng, duration, (int) Math.round(price));
            session.sendMessage(new TextMessage(MAPPER.writeValueAsString(dto)));
        } catch (IOException e) {
            logger.warn("[Simulation] Failed to send initial state: {}", e.getMessage());
        }
    }

    private void persistVehiclePosition(double lat, double lng) {
        try {
            vehicleRepository.updatePosition(vehicleId, lat, lng);
        } catch (Exception e) {
            logger.warn("[Simulation] Failed to update vehicle position: {}", e.getMessage());
        }
    }

    private double calculatePrice(int segIdx, int stepIdx, int stepsPerSeg,
                                  List<double[]> coords, double totalPrice) {
        double totalDist = 0;
        for (int i = 0; i < coords.size() - 1; i++)
            totalDist += dist(coords.get(i), coords.get(i + 1));
        if (totalDist == 0) return 0;

        double covered = 0;
        for (int i = 0; i < segIdx; i++)
            covered += dist(coords.get(i), coords.get(i + 1));

        double[] s = coords.get(segIdx);
        double[] e = coords.get(Math.min(segIdx + 1, coords.size() - 1));
        double dLat = (e[0] - s[0]) / stepsPerSeg * stepIdx;
        double dLng = (e[1] - s[1]) / stepsPerSeg * stepIdx;
        covered += dist(s, new double[]{s[0] + dLat, s[1] + dLng});

        return totalPrice * (covered / totalDist);
    }

    private int calculateRemainingDuration(int segIdx, int stepIdx, int totalSegs,
                                           int stepsPerSeg, double totalMinutes) {
        int current = segIdx * stepsPerSeg + stepIdx;
        int total   = totalSegs * stepsPerSeg;
        if (total == 0) return 0;
        double fractionLeft = 1.0 - ((double) current / total);
        return Math.max(0, (int) Math.round(fractionLeft * totalMinutes));
    }

    private double dist(double[] a, double[] b) {
        double R = 6371000;
        double dLat = Math.toRadians(b[0] - a[0]);
        double dLng = Math.toRadians(b[1] - a[1]);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a[0])) * Math.cos(Math.toRadians(b[0]))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }
}
