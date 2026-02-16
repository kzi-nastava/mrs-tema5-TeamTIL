package com.example.uberproject.dto.response;

public class ActiveHoursResponseDTO {
    private Double activeHoursLast24h;
    private String message;

    public ActiveHoursResponseDTO() {}

    public Double getActiveHoursLast24h() {
        return activeHoursLast24h;
    }

    public void setActiveHoursLast24h(Double activeHoursLast24h) {
        this.activeHoursLast24h = activeHoursLast24h;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

