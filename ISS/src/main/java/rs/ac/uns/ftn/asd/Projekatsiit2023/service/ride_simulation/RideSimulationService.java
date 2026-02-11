package rs.ac.uns.ftn.asd.Projekatsiit2023.service.ride_simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RideSimulationService {
    private final Map<Integer, RideSimulation> simulations = new ConcurrentHashMap<>();

    @Autowired
    private RideService rideService;
    @Autowired
    private RouteService routeService;

    @Autowired
    private RideRepository rideRepository;

    public synchronized void connectClient(int rideId, WebSocketSession session) throws IOException {
        RideSimulation sim = simulations.computeIfAbsent(rideId, id -> new RideSimulation(id, rideService, rideRepository, routeService));
        sim.addSession(session);
        session.sendMessage(new TextMessage(sim.getCurrentStateJson()));
    }
}
