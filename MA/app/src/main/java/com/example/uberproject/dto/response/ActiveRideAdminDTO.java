package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class ActiveRideAdminDTO {

    @SerializedName("rideId")
    private Integer rideId;

    // Driver info
    @SerializedName("driverFirstName")
    private String driverFirstName;

    @SerializedName("driverLastName")
    private String driverLastName;

    @SerializedName("driverEmail")
    private String driverEmail;

    @SerializedName("driverPhone")
    private String driverPhone;

    @SerializedName("driverProfilePicture")
    private String driverProfilePicture;

    @SerializedName("driverRating")
    private Double driverRating;

    // Vehicle info
    @SerializedName("vehicleModel")
    private String vehicleModel;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("licensePlate")
    private String licensePlate;

    // Passenger info
    @SerializedName("passengerFirstName")
    private String passengerFirstName;

    @SerializedName("passengerLastName")
    private String passengerLastName;

    @SerializedName("passengerPhone")
    private String passengerPhone;

    @SerializedName("passengerProfilePicture")
    private String passengerProfilePicture;

    // Ride info
    @SerializedName("startAddress")
    private String startAddress;

    @SerializedName("endAddress")
    private String endAddress;

    @SerializedName("rideStatus")
    private String rideStatus;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("estimatedEndTime")
    private String estimatedEndTime;

    @SerializedName("price")
    private Double price;

    @SerializedName("distanceKm")
    private Double distanceKm;

    // Vehicle location
    @SerializedName("vehicleLat")
    private Double vehicleLat;

    @SerializedName("vehicleLon")
    private Double vehicleLon;

    // Getters
    public Integer getRideId() { return rideId; }
    public String getDriverFirstName() { return driverFirstName; }
    public String getDriverLastName() { return driverLastName; }
    public String getDriverEmail() { return driverEmail; }
    public String getDriverPhone() { return driverPhone; }
    public String getDriverProfilePicture() { return driverProfilePicture; }
    public Double getDriverRating() { return driverRating; }
    public String getVehicleModel() { return vehicleModel; }
    public String getVehicleType() { return vehicleType; }
    public String getLicensePlate() { return licensePlate; }
    public String getPassengerFirstName() { return passengerFirstName; }
    public String getPassengerLastName() { return passengerLastName; }
    public String getPassengerPhone() { return passengerPhone; }
    public String getPassengerProfilePicture() { return passengerProfilePicture; }
    public String getStartAddress() { return startAddress; }
    public String getEndAddress() { return endAddress; }
    public String getRideStatus() { return rideStatus; }
    public String getStartTime() { return startTime; }
    public String getEstimatedEndTime() { return estimatedEndTime; }
    public Double getPrice() { return price; }
    public Double getDistanceKm() { return distanceKm; }
    public Double getVehicleLat() { return vehicleLat; }
    public Double getVehicleLon() { return vehicleLon; }

    // Helper
    public String getDriverFullName() {
        return (driverFirstName != null ? driverFirstName : "") + " "
                + (driverLastName != null ? driverLastName : "");
    }

    public String getPassengerFullName() {
        return (passengerFirstName != null ? passengerFirstName : "") + " "
                + (passengerLastName != null ? passengerLastName : "");
    }

    public String getFormattedRating() {
        if (driverRating == null) return "N/A";
        return String.format("%.1f", driverRating);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(rideStatus);
    }
}