package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideEstimationRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEstimationResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

@RestController
@RequestMapping("/api/route")
@Validated
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/estimate")
    public ResponseEntity<RideEstimationResponseDTO> estimateRoute(@Valid @RequestBody RideEstimationRequestDTO request) {
        if (request == null
                || request.getPickupAddress() == null || request.getPickupAddress().isBlank()
                || request.getDestinationAddress() == null || request.getDestinationAddress().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                request.getPickupLat(), request.getPickupLon(),
                request.getDestinationLat(), request.getDestinationLon());
        if (estimation == null) {
            return ResponseEntity.internalServerError().build();
        }

        RideEstimationResponseDTO response = getRideEstimationResponseDTO(request, estimation);

        return ResponseEntity.ok(response);
    }

    private static RideEstimationResponseDTO getRideEstimationResponseDTO(RideEstimationRequestDTO request, RouteService.RouteEstimation estimation) {
        double basePrice = estimation.distanceKm() * 120;
        double multiplier = switch (request.getVehicleType() == null ? "STANDARD" : request.getVehicleType().toUpperCase()) {
            case "LUXURY" -> 1.5;
            case "VAN" -> 1.3;
            default -> 1.0;
        };

        double finalPrice = Math.round(basePrice * multiplier * 100.0) / 100.0;
        String estimatedTime = String.format("%d min", (int) Math.round(estimation.durationMin()));

        return new RideEstimationResponseDTO(
                estimatedTime,
                Math.round(estimation.distanceKm() * 100.0) / 100.0,
                finalPrice,
                request.getVehicleType(),
                estimation.routeCoordinates()
        );
    }
}
