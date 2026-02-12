package com.example.uberproject.model;

import java.io.Serializable;

public class Ride implements Serializable {
    private final Integer id;
    private final String from;
    private final String to;
    private final String price;
    private final String status;
    private final String dateTime;
    private final Boolean panicSent;

    public Ride(int id, String from, String to, String price, String status, String dateTime) {
        this(id, from, to, price, status, dateTime, false);
    }

    public Ride(int id, String from, String to, String price, String status, String dateTime, Boolean panicSent) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.price = price;
        this.status = status;
        this.dateTime = dateTime;
        this.panicSent = panicSent != null ? panicSent : false;
    }

    public Integer getId() { return id; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getDateTime() { return dateTime; }

    public String getStatus() { return status; }

    public String getPrice() { return price; }

    public Boolean getPanicSent() { return panicSent; }
}
