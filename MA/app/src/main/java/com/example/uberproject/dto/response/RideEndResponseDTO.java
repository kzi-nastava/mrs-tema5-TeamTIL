package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class RideEndResponseDTO {
    @SerializedName("rideId")
    private Integer rideId;
    @SerializedName("finalLocation")
    private String finalLocation;
    @SerializedName("finalPrice")
    private Double finalPrice;
    @SerializedName("duration")
    private String duration;

    // Sledeća vožnja (null ako nema)
    @SerializedName("nextRideId")
    private Integer nextRideId;
    @SerializedName("nextRideFrom")
    private String nextRideFrom;
    @SerializedName("nextRideTo")
    private String nextRideTo;
    @SerializedName("nextRideScheduledTime")
    private String nextRideScheduledTime;
    @SerializedName("hasNextRide")
    private Boolean hasNextRide;

    public Integer getRideId() {
        return rideId;
    }

    public void setRideId(Integer rideId) {
        this.rideId = rideId;
    }

    public String getFinalLocation() {
        return finalLocation;
    }

    public void setFinalLocation(String finalLocation) {
        this.finalLocation = finalLocation;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getNextRideId() {
        return nextRideId;
    }

    public void setNextRideId(Integer nextRideId) {
        this.nextRideId = nextRideId;
    }

    public String getNextRideFrom() {
        return nextRideFrom;
    }

    public void setNextRideFrom(String nextRideFrom) {
        this.nextRideFrom = nextRideFrom;
    }

    public String getNextRideTo() {
        return nextRideTo;
    }

    public void setNextRideTo(String nextRideTo) {
        this.nextRideTo = nextRideTo;
    }

    public String getNextRideScheduledTime() {
        return nextRideScheduledTime;
    }

    public void setNextRideScheduledTime(String nextRideScheduledTime) {
        this.nextRideScheduledTime = nextRideScheduledTime;
    }

    public Boolean getHasNextRide() {
        return hasNextRide;
    }

    public void setHasNextRide(Boolean hasNextRide) {
        this.hasNextRide = hasNextRide;
    }
}
