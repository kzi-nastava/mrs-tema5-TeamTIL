package com.example.uberproject.dto.request;

import com.example.uberproject.model.Location;
import com.google.gson.annotations.SerializedName;

public class RideEndRequestDTO {
    @SerializedName("actualEndLocation")
    private Location actualEndLocation;

    public Location getActualEndLocation() {
        return actualEndLocation;
    }

    public void setActualEndLocation(Location actualEndLocation) {
        this.actualEndLocation = actualEndLocation;
    }
}
