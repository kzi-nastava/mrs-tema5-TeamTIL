package com.example.uberproject.models;

public class Ride {
    private final String from;
    private final String to;
    private final String price;
    private final String status;
    private final String dateTime;

    public Ride(String from, String to, String price, String status, String dateTime) {
        this.from = from;
        this.to = to;
        this.price = price;
        this.status = status;
        this.dateTime = dateTime;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getDateTime() { return dateTime; }

    public String getStatus() { return status; }

    public String getPrice() { return price; }
}
