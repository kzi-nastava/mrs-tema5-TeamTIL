package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class RideTrackingResponseDTO {

    @SerializedName("startAddress")
    private String startAddress;

    @SerializedName("startLatitude")
    private Double startLatitude;

    @SerializedName("startLongitude")
    private Double startLongitude;

    @SerializedName("endAddress")
    private String endAddress;

    @SerializedName("endLatitude")
    private Double endLatitude;

    @SerializedName("endLongitude")
    private Double endLongitude;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("driverName")
    private String driverName;

    @SerializedName("driverPhone")
    private String driverPhone;

    @SerializedName("passengerName")
    private String passengerName;

    @SerializedName("passengerPhone")
    private String passengerPhone;

    @SerializedName("vehicleType")
    private String vehicleType;

    // Getters
    public String getStartAddress() { return startAddress; }
    public Double getStartLatitude() { return startLatitude; }
    public Double getStartLongitude() { return startLongitude; }
    public String getEndAddress() { return endAddress; }
    public Double getEndLatitude() { return endLatitude; }
    public Double getEndLongitude() { return endLongitude; }
    public String getStartTime() { return startTime; }
    public String getDriverName() { return driverName; }
    public String getDriverPhone() { return driverPhone; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerPhone() { return passengerPhone; }
    public String getVehicleType() { return vehicleType; }
}