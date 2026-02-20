package com.example.uberproject.dto.response;

public class RideHistoryResponseDTO {
    private Integer id;
    private Integer routeId;
    private String passengerEmail;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerProfilePictureUrl;
    private String passengerPhoneNumber;
    private String driverEmail;
    private String driverFirstName;
    private String driverLastName;
    private String driverProfilePictureUrl;
    private String driverPhoneNumber;
    private String startLocation;
    private String endLocation;
    private String status;
    private String startTime;
    private String estimatedEndTime;
    private Double price;
    private Double distance;
    private Double duration;
    private Boolean panicSent;
    private Boolean rated;
    private String vehicleModel;
    private String vehicleLicensePlate;

    public RideHistoryResponseDTO() {}

    public RideHistoryResponseDTO(Integer id, String passengerEmail, String passengerFirstName,
                                  String passengerLastName, String passengerProfilePictureUrl, String passengerPhoneNumber,
                                  String driverEmail, String driverFirstName, String driverLastName,
                                  String driverProfilePictureUrl, String driverPhoneNumber,
                                  String startLocation, String endLocation, String status,
                                  String startTime, String estimatedEndTime, Double price,
                                  Double distance, Double duration, Boolean panicSent, Boolean rated,
                                  String vehicleModel, String vehicleLicensePlate) {
        this.id = id;
        this.passengerEmail = passengerEmail;
        this.passengerFirstName = passengerFirstName;
        this.passengerLastName = passengerLastName;
        this.passengerProfilePictureUrl = passengerProfilePictureUrl;
        this.passengerPhoneNumber = passengerPhoneNumber;
        this.driverEmail = driverEmail;
        this.driverFirstName = driverFirstName;
        this.driverLastName = driverLastName;
        this.driverProfilePictureUrl = driverProfilePictureUrl;
        this.driverPhoneNumber = driverPhoneNumber;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.status = status;
        this.startTime = startTime;
        this.estimatedEndTime = estimatedEndTime;
        this.price = price;
        this.distance = distance;
        this.duration = duration;
        this.panicSent = panicSent;
        this.rated = rated;
        this.vehicleModel = vehicleModel;
        this.vehicleLicensePlate = vehicleLicensePlate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }

    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }
    public String getPassengerFirstName() { return passengerFirstName; }
    public void setPassengerFirstName(String passengerFirstName) { this.passengerFirstName = passengerFirstName; }
    public String getPassengerLastName() { return passengerLastName; }
    public void setPassengerLastName(String passengerLastName) { this.passengerLastName = passengerLastName; }
    public String getPassengerProfilePictureUrl() { return passengerProfilePictureUrl; }
    public void setPassengerProfilePictureUrl(String passengerProfilePictureUrl) { this.passengerProfilePictureUrl = passengerProfilePictureUrl; }
    public String getPassengerPhoneNumber() { return passengerPhoneNumber; }
    public void setPassengerPhoneNumber(String passengerPhoneNumber) { this.passengerPhoneNumber = passengerPhoneNumber; }
    public String getDriverEmail() { return driverEmail; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
    public String getDriverFirstName() { return driverFirstName; }
    public void setDriverFirstName(String driverFirstName) { this.driverFirstName = driverFirstName; }
    public String getDriverLastName() { return driverLastName; }
    public void setDriverLastName(String driverLastName) { this.driverLastName = driverLastName; }
    public String getDriverProfilePictureUrl() { return driverProfilePictureUrl; }
    public void setDriverProfilePictureUrl(String driverProfilePictureUrl) { this.driverProfilePictureUrl = driverProfilePictureUrl; }
    public String getDriverPhoneNumber() { return driverPhoneNumber; }
    public void setDriverPhoneNumber(String driverPhoneNumber) { this.driverPhoneNumber = driverPhoneNumber; }
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEstimatedEndTime() { return estimatedEndTime; }
    public void setEstimatedEndTime(String estimatedEndTime) { this.estimatedEndTime = estimatedEndTime; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }
    public Boolean getPanicSent() { return panicSent; }
    public void setPanicSent(Boolean panicSent) { this.panicSent = panicSent; }
    public Boolean getRated() { return rated; }
    public void setRated(Boolean rated) { this.rated = rated; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getVehicleLicensePlate() { return vehicleLicensePlate; }
    public void setVehicleLicensePlate(String vehicleLicensePlate) { this.vehicleLicensePlate = vehicleLicensePlate; }
}
