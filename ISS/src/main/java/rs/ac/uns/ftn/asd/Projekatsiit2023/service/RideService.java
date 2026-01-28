package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.AssignedRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.RideHistoryDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.AdminRideResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCancelResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PriceConfig;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PanicNotificationRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PriceConfigRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RideService {
    @Autowired
    private RouteService routeService;

    @Autowired
    private PriceConfigRepository priceConfigRepository;
    @Autowired
    private PanicNotificationRepository panicNotificationRepository;

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional
    public RideCancelResponseDTO cancelRide(Integer rideId, String reason) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if (RideStatus.CANCELED.equals(ride.getRideStatus()) || RideStatus.FINISHED.equals(ride.getRideStatus())) {
            throw new RuntimeException("Cannot cancel ride with status: " + ride.getRideStatus());
        }

        ride.setRideStatus(RideStatus.CANCELED);
        ride.setCancellationReason(reason != null ? reason : "User cancelled");

        Ride savedRide = rideRepository.save(ride);

        return new RideCancelResponseDTO(
                savedRide.getId(),
                savedRide.getRideStatus().name(),
                savedRide.getCancellationReason(),
                "Ride cancelled successfully"
        );
    }

    public AssignedRideDTO mapRideToDTO(Ride ride, UserType userType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

        // Estimate route
        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                ride.getStartLocation().getLatitude(),
                ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getLatitude(),
                ride.getEndLocation().getLongitude()
        );

        double distance = estimation != null ? estimation.distanceKm : 0.0;
        double duration = estimation != null ? estimation.durationMin : 0.0;

        String estimatedEndTime = null;
        if (ride.getStartTime() != null && duration > 0) {
            estimatedEndTime = ride.getStartTime().plusMinutes((long) duration).format(formatter);
        }

        double price = calculateFinalPrice(ride.getDriver().getVehicle().getType(), ride.getStartLocation(), ride.getEndLocation());

        String accountEmail = userType == UserType.DRIVER
                ? ride.getDriver().getEmail()
                : ride.getPassenger().getEmail();

        return new AssignedRideDTO(
                ride.getId(),
                accountEmail,
                ride.getStartLocation().getAddress(),
                ride.getEndLocation() != null ? ride.getEndLocation().getAddress() : "",
                ride.getRideStatus().toString(),
                ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                estimatedEndTime,
                price,
                distance,
                duration
        );
    }

    public List<AdminRideResponseDTO> getAllRidesWithPanicInfoForAdmin() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        List<RideStatus> statuses = List.of(RideStatus.FINISHED, RideStatus.CANCELED);
        List<Ride> rides = rideRepository.findByRideStatusIn(statuses);

        return rides.stream().map(ride -> {
            boolean panicSent = panicNotificationRepository.existsByRideId(ride.getId());

            // Route estimation
            RouteService.RouteEstimation estimation = routeService.estimateRoute(
                    ride.getStartLocation().getLatitude(),
                    ride.getStartLocation().getLongitude(),
                    ride.getEndLocation().getLatitude(),
                    ride.getEndLocation().getLongitude()
            );
            double distance = estimation != null ? estimation.distanceKm : 0.0;
            double duration = estimation != null ? estimation.durationMin : 0.0;

            String estimatedEndTime = null;
            if (ride.getStartTime() != null && duration > 0) {
                estimatedEndTime = ride.getStartTime().plusMinutes((long) duration).format(formatter);
            }

            double price = calculateFinalPrice(ride.getDriver().getVehicle().getType(), ride.getStartLocation(), ride.getEndLocation());

            return new AdminRideResponseDTO(
                    ride.getId(),
                    ride.getPassenger().getEmail(),
                    ride.getDriver().getEmail(),
                    ride.getStartLocation().getAddress(),
                    ride.getEndLocation() != null ? ride.getEndLocation().getAddress() : "",
                    ride.getRideStatus().toString(),
                    ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                    estimatedEndTime,
                    price,
                    distance,
                    duration,
                    panicSent
            );
        }).collect(Collectors.toList());
    }

    public double calculateFinalPrice(VehicleType vehicleType, Location start, Location end) {
        PriceConfig priceConfig = priceConfigRepository.findByVehicleType(vehicleType)
                .orElseThrow(() -> new RuntimeException("Price config not found"));

        double distanceKm = calculateDistanceKm(start, end);
        return priceConfig.getBasePrice() + distanceKm * priceConfig.getPricePerKm();
    }

    public double calculateDistanceKm(Location start, Location end) {
        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude()
        );
        return estimation != null ? estimation.distanceKm : 0.0;
    }
}
