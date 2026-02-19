package com.example.uberproject.dto.response;

public class AddToFavoritesResponseDTO {
    private Integer routeId;
    private String message;
    private Boolean success;

    public AddToFavoritesResponseDTO() {}

    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
}
