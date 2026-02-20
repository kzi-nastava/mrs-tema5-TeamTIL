package com.example.uberproject.model;

import java.util.Objects;

public class Ride implements java.io.Serializable {
    private Integer id;
    private Integer routeId;
    private String from;
    private String to;
    private String price;
    private String status;
    private String dateTime;
    private Boolean panicSent;

    public Ride() {}


    public Ride(Integer id, String from, String to, String price,
                String status, String dateTime, Boolean panicSent) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.price = price;
        this.status = status;
        this.dateTime = dateTime;
        this.panicSent = panicSent;
    }


    public Ride(Integer id, Integer routeId, String from, String to, String price,
                String status, String dateTime, Boolean panicSent) {
        this.id = id;
        this.routeId = routeId;
        this.from = from;
        this.to = to;
        this.price = price;
        this.status = status;
        this.dateTime = dateTime;
        this.panicSent = panicSent;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public Boolean getPanicSent() { return panicSent; }
    public void setPanicSent(Boolean panicSent) { this.panicSent = panicSent; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ride)) return false;
        Ride ride = (Ride) o;
        return Objects.equals(id, ride.id)
                && Objects.equals(routeId, ride.routeId)
                && Objects.equals(status, ride.status)
                && Objects.equals(price, ride.price);
    }

    @Override
    public int hashCode() { return Objects.hash(id, routeId, status, price); }
}
