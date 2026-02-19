package com.example.uberproject.dto.request;

import com.example.uberproject.model.Location;
import java.time.LocalDateTime;

public class RideStopRequestDTO {
    private Location actualEndLocation;
    private LocalDateTime actualEndTime;

    public RideStopRequestDTO() {}

    public RideStopRequestDTO(Location actualEndLocation, LocalDateTime actualEndTime) {
        this.actualEndLocation = actualEndLocation;
        this.actualEndTime = actualEndTime;
    }

    public Location getActualEndLocation() {
        return actualEndLocation;
    }

    public void setActualEndLocation(Location actualEndLocation) {
        this.actualEndLocation = actualEndLocation;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }
}

