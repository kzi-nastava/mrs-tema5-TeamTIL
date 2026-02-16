package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideEstimationRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEstimationResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.FavoriteRouteDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.AddToFavoritesResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

import java.security.Principal;
import java.util.List;

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

    // Dodaj u omiljene
    @PostMapping("/{routeId}/favorite")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<AddToFavoritesResponseDTO> addToFavorites(
            @PathVariable Integer routeId,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = principal.getName();
        AddToFavoritesResponseDTO response = routeService.addToFavorites(routeId, userEmail);
        return ResponseEntity.ok(response);
    }

    // Ukloni iz omiljenih
    @DeleteMapping("/{routeId}/favorite")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<AddToFavoritesResponseDTO> removeFromFavorites(
            @PathVariable Integer routeId,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = principal.getName();
        AddToFavoritesResponseDTO response = routeService.removeFromFavorites(routeId, userEmail);
        return ResponseEntity.ok(response);
    }

    // Lista omiljenih ruta
    @GetMapping("/favorites")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<List<FavoriteRouteDTO>> getFavoriteRoutes(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = principal.getName();
        List<FavoriteRouteDTO> favorites = routeService.getFavoriteRoutes(userEmail);
        return ResponseEntity.ok(favorites);
    }

    // Proveri da li je ruta omiljena
    @GetMapping("/{routeId}/favorite/check")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Integer routeId,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = principal.getName();
        boolean isFavorite = routeService.isRouteFavorite(routeId, userEmail);
        return ResponseEntity.ok(isFavorite);
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