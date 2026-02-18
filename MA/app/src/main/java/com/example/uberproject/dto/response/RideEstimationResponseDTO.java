package com.example.uberproject.dto.response;

import java.util.List;

public class RideEstimationResponseDTO {
    private String estimatedTime;
    private double estimatedDistance;
    private double estimatedPrice;
    private String vehicleType;
    private List<List<Double>> routeCoordinates;

    public RideEstimationResponseDTO() {}

    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public double getEstimatedDistance() { return estimatedDistance; }
    public void setEstimatedDistance(double estimatedDistance) { this.estimatedDistance = estimatedDistance; }

    public double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public List<List<Double>> getRouteCoordinates() { return routeCoordinates; }
    public void setRouteCoordinates(List<List<Double>> routeCoordinates) { this.routeCoordinates = routeCoordinates; }
}

