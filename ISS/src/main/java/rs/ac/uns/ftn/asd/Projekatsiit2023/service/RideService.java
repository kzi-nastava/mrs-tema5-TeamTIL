package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCancelResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;

@Service
public class RideService {

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
}
