package com.example.uberproject.dto.response;

import java.util.List;

public class FavoriteRouteDTO {
    private Integer routeId;
    private String startLocation;
    private String endLocation;
    private List<String> intermediateStops;
    private Double distanceKm;
    private Double estimatedTimeMin;

    public FavoriteRouteDTO() {}

    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public List<String> getIntermediateStops() { return intermediateStops; }
    public void setIntermediateStops(List<String> intermediateStops) { this.intermediateStops = intermediateStops; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getEstimatedTimeMin() { return estimatedTimeMin; }
    public void setEstimatedTimeMin(Double estimatedTimeMin) { this.estimatedTimeMin = estimatedTimeMin; }
}
