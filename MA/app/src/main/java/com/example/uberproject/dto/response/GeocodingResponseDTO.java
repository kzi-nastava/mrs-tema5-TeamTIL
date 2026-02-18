package com.example.uberproject.dto.response;

public class GeocodingResponseDTO {
    private double lat;
    private double lon;
    private String name;

    public GeocodingResponseDTO() {}

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

