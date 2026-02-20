package rs.ac.uns.ftn.asd.Projekatsiit2023.service.ride_simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PriceConfigRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.VehicleRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RideSimulationService {
    private static final Logger logger = LoggerFactory.getLogger(RideSimulationService.class);

    private final Map<Integer, RideSimulation> simulations = new ConcurrentHashMap<>();

    @Autowired
    private RideService rideService;
    @Autowired
    private RouteService routeService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PriceConfigRepository priceConfigRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // ─── Called by RideService.startRide() ───────────────────────────────────────

    /**
     * Creates and starts the simulation for a ride.
     * Must be called when the driver marks the ride as IN_PROGRESS.
     */
    public synchronized void startSimulation(int rideId) {
        if (simulations.containsKey(rideId)) {
            logger.warn("[SimService] Simulation already exists for rideId={}", rideId);
            return;
        }
        try {
            RideSimulation sim = new RideSimulation(rideId, rideService, rideRepository,
                    routeService, priceConfigRepository, vehicleRepository);
            simulations.put(rideId, sim);
            sim.start();
            logger.info("[SimService] Simulation created and started for rideId={}", rideId);
        } catch (Exception e) {
            logger.error("[SimService] Failed to start simulation for rideId={}: {}", rideId, e.getMessage());
        }
    }

    // ─── Called by RideService.endRide() / stopRide() ────────────────────────────

    /**
     * Stops the simulation, broadcasts RIDE_ENDED to all connected WebSocket clients,
     * and removes it from the registry.
     */
    public void stopSimulation(int rideId) {
        RideSimulation sim = simulations.remove(rideId);
        if (sim != null) {
            sim.stopAndNotify();
            logger.info("[SimService] Simulation stopped for rideId={}", rideId);
        }
    }

    // ─── Called by WebSocket handler when a client connects ──────────────────────

    /**
     * Attaches a WebSocket session to an existing (running) simulation.
     * If no simulation exists yet (e.g. client connected before startRide), sends an error.
     */
    public synchronized void connectClient(int rideId, WebSocketSession session) throws IOException {
        RideSimulation sim = simulations.get(rideId);
        if (sim == null) {
            // Ride not started or already finished – tell the client
            session.sendMessage(new TextMessage("{\"type\":\"RIDE_NOT_ACTIVE\"}"));
            session.close();
            logger.warn("[SimService] No active simulation for rideId={}, closing client session", rideId);
            return;
        }
        sim.addSession(session);
        logger.info("[SimService] Client connected to rideId={}", rideId);
    }

    /**
     * Returns the last known vehicle position for a ride.
     * Used by endRide / stopRide to get real coordinates instead of hardcoded ones.
     */
    public double[] getCurrentPosition(int rideId) {
        RideSimulation sim = simulations.get(rideId);
        if (sim == null) return null;
        return new double[]{sim.getCurrentLatitude(), sim.getCurrentLongitude()};
    }

    public boolean hasSimulation(int rideId) {
        return simulations.containsKey(rideId);
    }
}
