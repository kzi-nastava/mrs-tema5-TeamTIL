package com.example.uberproject.dto.request;

import com.example.uberproject.model.Location;

public class RideStopRequestDTO {
    private Location actualEndLocation;
    private String actualEndTime;

    public RideStopRequestDTO() {}

    public RideStopRequestDTO(Location actualEndLocation, String actualEndTime) {
        this.actualEndLocation = actualEndLocation;
        this.actualEndTime = actualEndTime;
    }

    public Location getActualEndLocation() {
        return actualEndLocation;
    }

    public void setActualEndLocation(Location actualEndLocation) {
        this.actualEndLocation = actualEndLocation;
    }

    public String getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(String actualEndTime) {
        this.actualEndTime = actualEndTime;
    }
}

