package com.example.uberproject.dto.request;

import java.util.List;

public class RideRequestDTO {
    private List<LocationDTO> locations;
    private List<String> passengerEmails;
    private String vehicleType;
    private Boolean babyFriendly;
    private Boolean petFriendly;
    private String scheduledTime;

    public RideRequestDTO() {}

    public RideRequestDTO(List<LocationDTO> locations, List<String> passengerEmails,
                          String vehicleType, Boolean babyFriendly, Boolean petFriendly,
                          String scheduledTime) {
        this.locations = locations;
        this.passengerEmails = passengerEmails;
        this.vehicleType = vehicleType;
        this.babyFriendly = babyFriendly;
        this.petFriendly = petFriendly;
        this.scheduledTime = scheduledTime;
    }

    public static class LocationDTO {
        private String address;
        private Double latitude;
        private Double longitude;

        public LocationDTO() {}

        public LocationDTO(String address, Double latitude, Double longitude) {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    public List<LocationDTO> getLocations() { return locations; }
    public void setLocations(List<LocationDTO> locations) { this.locations = locations; }
    public List<String> getPassengerEmails() { return passengerEmails; }
    public void setPassengerEmails(List<String> passengerEmails) { this.passengerEmails = passengerEmails; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public Boolean getBabyFriendly() { return babyFriendly; }
    public void setBabyFriendly(Boolean babyFriendly) { this.babyFriendly = babyFriendly; }
    public Boolean getPetFriendly() { return petFriendly; }
    public void setPetFriendly(Boolean petFriendly) { this.petFriendly = petFriendly; }
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
}
