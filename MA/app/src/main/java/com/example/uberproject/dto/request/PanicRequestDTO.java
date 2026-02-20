package com.example.uberproject.dto.request;

public class PanicRequestDTO {
    private Integer rideId;
    private Integer locationId;
    private Double latitude;
    private Double longitude;

    public PanicRequestDTO() {}

    public PanicRequestDTO(Integer rideId, Integer locationId, Double latitude, Double longitude) {
        this.rideId = rideId;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getRideId() {
        return rideId;
    }

    public void setRideId(Integer rideId) {
        this.rideId = rideId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

