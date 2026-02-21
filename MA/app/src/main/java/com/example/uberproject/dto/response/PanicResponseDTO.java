package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class PanicResponseDTO {
    @SerializedName(value = "panicId", alternate = {"id"})
    private Integer id;

    private Integer rideId;
    private Integer locationId;
    private Integer registeredUserId;
    private Integer driverId;
    private Boolean handled;
    private String timestamp;  // Changed from LocalTime to String
    private String reportedBy;

    // Vehicle information
    private String vehicleName;
    private String vehicleLicensePlate;

    // Location information
    private String locationAddress;
    private Double latitude;
    private Double longitude;

    public PanicResponseDTO() {}

    public PanicResponseDTO(Integer id, Integer rideId, Integer locationId, Integer registeredUserId,
                            Integer driverId, Boolean handled, String timestamp, String reportedBy,
                            String vehicleName, String vehicleLicensePlate, String locationAddress,
                            Double latitude, Double longitude) {
        this.id = id;
        this.rideId = rideId;
        this.locationId = locationId;
        this.registeredUserId = registeredUserId;
        this.driverId = driverId;
        this.handled = handled;
        this.timestamp = timestamp;
        this.reportedBy = reportedBy;
        this.vehicleName = vehicleName;
        this.vehicleLicensePlate = vehicleLicensePlate;
        this.locationAddress = locationAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Convenience alias – backend @JsonProperty exposes "panicId" but field is "id"
    public Integer getPanicId() { return id; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getRegisteredUserId() {
        return registeredUserId;
    }

    public void setRegisteredUserId(Integer registeredUserId) {
        this.registeredUserId = registeredUserId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public Boolean getHandled() {
        return handled;
    }

    public void setHandled(Boolean handled) {
        this.handled = handled;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehicleLicensePlate() {
        return vehicleLicensePlate;
    }

    public void setVehicleLicensePlate(String vehicleLicensePlate) {
        this.vehicleLicensePlate = vehicleLicensePlate;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
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

