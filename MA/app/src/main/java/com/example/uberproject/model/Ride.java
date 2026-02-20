package com.example.uberproject.model;

import java.util.Objects;

public class Ride implements java.io.Serializable {
    private Integer id;
    private Integer routeId;
    private String from;
    private String to;
    private String price;
    private String status;
    private String dateTime;
    private Boolean panicSent;
    // Driver info
    private String driverFirstName;
    private String driverLastName;
    private String driverPhoneNumber;
    private String driverProfilePictureUrl;
    // Ride details
    private String startTime;
    private String estimatedEndTime;
    private Double distance;
    private Double duration;
    // Passenger info
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerPhoneNumber;
    private String passengerProfilePictureUrl;
    private String passengerEmail;
    // Vehicle info
    private String vehicleModel;
    private String vehicleLicensePlate;

    public Ride() {}


    public Ride(Integer id, String from, String to, String price,
                String status, String dateTime, Boolean panicSent) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.price = price;
        this.status = status;
        this.dateTime = dateTime;
        this.panicSent = panicSent;
    }


    public Ride(Integer id, Integer routeId, String from, String to, String price,
                String status, String dateTime, Boolean panicSent) {
        this.id = id;
        this.routeId = routeId;
        this.from = from;
        this.to = to;
        this.price = price;
        this.status = status;
        this.dateTime = dateTime;
        this.panicSent = panicSent;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public Boolean getPanicSent() { return panicSent; }
    public void setPanicSent(Boolean panicSent) { this.panicSent = panicSent; }

    public String getDriverFirstName() { return driverFirstName; }
    public void setDriverFirstName(String driverFirstName) { this.driverFirstName = driverFirstName; }
    public String getDriverLastName() { return driverLastName; }
    public void setDriverLastName(String driverLastName) { this.driverLastName = driverLastName; }
    public String getDriverPhoneNumber() { return driverPhoneNumber; }
    public void setDriverPhoneNumber(String driverPhoneNumber) { this.driverPhoneNumber = driverPhoneNumber; }
    public String getDriverProfilePictureUrl() { return driverProfilePictureUrl; }
    public void setDriverProfilePictureUrl(String url) { this.driverProfilePictureUrl = url; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEstimatedEndTime() { return estimatedEndTime; }
    public void setEstimatedEndTime(String estimatedEndTime) { this.estimatedEndTime = estimatedEndTime; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }

    public String getPassengerFirstName() { return passengerFirstName; }
    public void setPassengerFirstName(String v) { this.passengerFirstName = v; }
    public String getPassengerLastName() { return passengerLastName; }
    public void setPassengerLastName(String v) { this.passengerLastName = v; }
    public String getPassengerPhoneNumber() { return passengerPhoneNumber; }
    public void setPassengerPhoneNumber(String v) { this.passengerPhoneNumber = v; }
    public String getPassengerProfilePictureUrl() { return passengerProfilePictureUrl; }
    public void setPassengerProfilePictureUrl(String v) { this.passengerProfilePictureUrl = v; }
    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String v) { this.passengerEmail = v; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String v) { this.vehicleModel = v; }
    public String getVehicleLicensePlate() { return vehicleLicensePlate; }
    public void setVehicleLicensePlate(String v) { this.vehicleLicensePlate = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ride)) return false;
        Ride ride = (Ride) o;
        return Objects.equals(id, ride.id)
                && Objects.equals(routeId, ride.routeId)
                && Objects.equals(status, ride.status)
                && Objects.equals(price, ride.price);
    }

    @Override
    public int hashCode() { return Objects.hash(id, routeId, status, price); }
}
