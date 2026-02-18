package com.example.uberproject.dto.request;

public class RideEstimationRequestDTO {
    private String pickupAddress;
    private String destinationAddress;
    private String vehicleType;
    private double pickupLat;
    private double pickupLon;
    private double destinationLat;
    private double destinationLon;

    public RideEstimationRequestDTO() {
    }

    public RideEstimationRequestDTO(String pickupAddress, String destinationAddress, String vehicleType,
                                    double pickupLat, double pickupLon, double destinationLat, double destinationLon) {
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
        this.vehicleType = vehicleType;
        this.pickupLat = pickupLat;
        this.pickupLon = pickupLon;
        this.destinationLat = destinationLat;
        this.destinationLon = destinationLon;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public double getPickupLon() {
        return pickupLon;
    }

    public void setPickupLon(double pickupLon) {
        this.pickupLon = pickupLon;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLon() {
        return destinationLon;
    }

    public void setDestinationLon(double destinationLon) {
        this.destinationLon = destinationLon;
    }
}

