package com.example.uberproject.dto.response;

public class RideStatsDayDTO {
    private String date;
    private int ridesCount;
    private double distanceKm;
    private double moneyAmount;

    public RideStatsDayDTO() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getRidesCount() { return ridesCount; }
    public void setRidesCount(int ridesCount) { this.ridesCount = ridesCount; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getMoneyAmount() { return moneyAmount; }
    public void setMoneyAmount(double moneyAmount) { this.moneyAmount = moneyAmount; }
}
