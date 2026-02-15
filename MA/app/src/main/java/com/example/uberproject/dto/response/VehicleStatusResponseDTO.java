package com.example.uberproject.dto.response;

public class VehicleStatusResponseDTO {
    private String name;
    private String type;
    private String licensePlate;
    private Boolean available;
    private Double latitude;
    private Double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
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

    public VehicleStatusResponseDTO(String name, String type, String licensePlate, Boolean available, Double latitude, Double longitude) {
        this.name = name;
        this.type = type;
        this.licensePlate = licensePlate;
        this.available = available;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public VehicleStatusResponseDTO() {
    }
}
