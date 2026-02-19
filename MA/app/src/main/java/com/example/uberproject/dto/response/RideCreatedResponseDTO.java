package com.example.uberproject.dto.response;

public class RideCreatedResponseDTO {
    private Integer id;
    private String status;
    private Double price;
    private String driverName;
    private String driverEmail;
    private String vehicleInfo;
    private String message;
    private String scheduledTime;
    private String estimatedEndTime;
    private Double distanceKm;
    private Double durationMin;

    public RideCreatedResponseDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverEmail() { return driverEmail; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getEstimatedEndTime() { return estimatedEndTime; }
    public void setEstimatedEndTime(String estimatedEndTime) { this.estimatedEndTime = estimatedEndTime; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getDurationMin() { return durationMin; }
    public void setDurationMin(Double durationMin) { this.durationMin = durationMin; }
}
