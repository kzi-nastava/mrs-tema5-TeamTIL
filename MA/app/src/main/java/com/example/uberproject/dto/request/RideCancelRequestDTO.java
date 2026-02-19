package com.example.uberproject.dto.request;

public class RideCancelRequestDTO {
    private String cancellationReason;

    public RideCancelRequestDTO() {}

    public RideCancelRequestDTO(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}

