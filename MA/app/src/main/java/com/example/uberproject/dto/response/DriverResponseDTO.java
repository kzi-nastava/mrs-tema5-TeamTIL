package com.example.uberproject.dto.response;

public class DriverResponseDTO extends UserResponseDTO {
    private String vehicleModel;
    private String vehicleType;
    private String licensePlate;
    private Integer passengerCapacity;
    private Boolean babyFriendly;
    private Boolean petFriendly;
    private Boolean isActive;
    private Double activeHours;
    private Boolean isBlocked;
    private String blockReason;

    public String getVehicleModel() { return vehicleModel; }
    public String getVehicleType() { return vehicleType; }
    public String getLicensePlate() { return licensePlate; }
    public Integer getPassengerCapacity() { return passengerCapacity; }
    public Boolean getBabyFriendly() { return babyFriendly; }
    public Boolean getPetFriendly() { return petFriendly; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public Double getActiveHours() { return activeHours; }
    public void setActiveHours(Double activeHours) { this.activeHours = activeHours; }
    public Boolean getIsBlocked() { return isBlocked; }
    public String getBlockReason() { return blockReason; }
}