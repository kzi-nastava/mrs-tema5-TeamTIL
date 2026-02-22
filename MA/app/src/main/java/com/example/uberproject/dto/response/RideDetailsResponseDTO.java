package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RideDetailsResponseDTO {
    @SerializedName("id")
    private Integer id;
    @SerializedName("passengerFirstName")
    private String passengerFirstName;
    @SerializedName("passengerLastName")
    private String passengerLastName;
    @SerializedName("passengerProfilePictureUrl")
    private String passengerProfilePictureUrl;
    @SerializedName("passengerPhoneNumber")
    private String passengerPhoneNumber;

    @SerializedName("driverFirstName")
    private String driverFirstName;
    @SerializedName("driverLastName")
    private String driverLastName;
    @SerializedName("driverProfilePictureUrl")
    private String driverProfilePictureUrl;
    @SerializedName("driverPhoneNumber")
    private String driverPhoneNumber;
    @SerializedName("driverRating")
    private Double driverRating;

    @SerializedName("route")
    private List<LocationResponseDTO> route;
    @SerializedName("linkedPassengers")
    private List<LinkdPassengerDTO> linkedPassengers;

    @SerializedName("status")
    private String status;
    @SerializedName("startTime")
    private String startTime;
    @SerializedName("estimatedEndTime")
    private String estimatedEndTime;

    @SerializedName("price")
    private Double price;
    @SerializedName("distance")
    private Double distance;
    @SerializedName("duration")
    private Double duration;

    @SerializedName("rideRating")
    private Double rideRating;
    @SerializedName("rideComment")
    private String rideComment;

    @SerializedName("panicSent")
    private Boolean panicSent;
    @SerializedName("reportedIssues")
    private List<String> reportedIssues;

    @SerializedName("vehicleModel")
    private String vehicleModel;
    @SerializedName("vehicleLicensePlate")
    private String vehicleLicensePlate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassengerFirstName() {
        return passengerFirstName;
    }

    public void setPassengerFirstName(String passengerFirstName) {
        this.passengerFirstName = passengerFirstName;
    }

    public String getPassengerLastName() {
        return passengerLastName;
    }

    public void setPassengerLastName(String passengerLastName) {
        this.passengerLastName = passengerLastName;
    }

    public String getPassengerProfilePictureUrl() {
        return passengerProfilePictureUrl;
    }

    public void setPassengerProfilePictureUrl(String passengerProfilePictureUrl) {
        this.passengerProfilePictureUrl = passengerProfilePictureUrl;
    }

    public String getPassengerPhoneNumber() {
        return passengerPhoneNumber;
    }

    public void setPassengerPhoneNumber(String passengerPhoneNumber) {
        this.passengerPhoneNumber = passengerPhoneNumber;
    }

    public String getDriverFirstName() {
        return driverFirstName;
    }

    public void setDriverFirstName(String driverFirstName) {
        this.driverFirstName = driverFirstName;
    }

    public String getDriverLastName() {
        return driverLastName;
    }

    public void setDriverLastName(String driverLastName) {
        this.driverLastName = driverLastName;
    }

    public String getDriverProfilePictureUrl() {
        return driverProfilePictureUrl;
    }

    public void setDriverProfilePictureUrl(String driverProfilePictureUrl) {
        this.driverProfilePictureUrl = driverProfilePictureUrl;
    }

    public String getDriverPhoneNumber() {
        return driverPhoneNumber;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        this.driverPhoneNumber = driverPhoneNumber;
    }

    public Double getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(Double driverRating) {
        this.driverRating = driverRating;
    }

    public List<LocationResponseDTO> getRoute() {
        return route;
    }

    public void setRoute(List<LocationResponseDTO> route) {
        this.route = route;
    }

    public List<LinkdPassengerDTO> getLinkedPassengers() {
        return linkedPassengers;
    }

    public void setLinkedPassengers(List<LinkdPassengerDTO> linkedPassengers) {
        this.linkedPassengers = linkedPassengers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEstimatedEndTime() {
        return estimatedEndTime;
    }

    public void setEstimatedEndTime(String estimatedEndTime) {
        this.estimatedEndTime = estimatedEndTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Double getRideRating() {
        return rideRating;
    }

    public void setRideRating(Double rideRating) {
        this.rideRating = rideRating;
    }

    public String getRideComment() {
        return rideComment;
    }

    public void setRideComment(String rideComment) {
        this.rideComment = rideComment;
    }

    public Boolean getPanicSent() {
        return panicSent;
    }

    public void setPanicSent(Boolean panicSent) {
        this.panicSent = panicSent;
    }

    public List<String> getReportedIssues() {
        return reportedIssues;
    }

    public void setReportedIssues(List<String> reportedIssues) {
        this.reportedIssues = reportedIssues;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleLicensePlate() {
        return vehicleLicensePlate;
    }

    public void setVehicleLicensePlate(String vehicleLicensePlate) {
        this.vehicleLicensePlate = vehicleLicensePlate;
    }
}