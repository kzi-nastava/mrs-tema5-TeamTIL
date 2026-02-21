package com.example.uberproject.dto.response;

import java.util.List;

public class RideStatsResponseDTO {
    private List<RideStatsDayDTO> days;
    private int totalRides;
    private double totalDistanceKm;
    private double totalMoney;
    private double avgRidesPerDay;
    private double avgDistancePerDay;
    private double avgMoneyPerDay;

    public RideStatsResponseDTO() {}

    public List<RideStatsDayDTO> getDays() { return days; }
    public void setDays(List<RideStatsDayDTO> days) { this.days = days; }

    public int getTotalRides() { return totalRides; }
    public void setTotalRides(int totalRides) { this.totalRides = totalRides; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public double getTotalMoney() { return totalMoney; }
    public void setTotalMoney(double totalMoney) { this.totalMoney = totalMoney; }

    public double getAvgRidesPerDay() { return avgRidesPerDay; }
    public void setAvgRidesPerDay(double avgRidesPerDay) { this.avgRidesPerDay = avgRidesPerDay; }

    public double getAvgDistancePerDay() { return avgDistancePerDay; }
    public void setAvgDistancePerDay(double avgDistancePerDay) { this.avgDistancePerDay = avgDistancePerDay; }

    public double getAvgMoneyPerDay() { return avgMoneyPerDay; }
    public void setAvgMoneyPerDay(double avgMoneyPerDay) { this.avgMoneyPerDay = avgMoneyPerDay; }
}
