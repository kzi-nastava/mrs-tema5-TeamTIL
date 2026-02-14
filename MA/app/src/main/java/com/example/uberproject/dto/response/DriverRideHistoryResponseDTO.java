package com.example.uberproject.dto.response;

import java.io.Serializable;
import java.util.List;

public class DriverRideHistoryResponseDTO implements Serializable {
    private Integer id;
    private List<PassengerDTO> passengers;

    private String from;
    private String to;
    private String status;
    private String canceledBy;

    private String date;
    private String startTime;
    private String endTime;

    private String price;
    private String duration;
    private String distance;

    private Boolean panicSent;

    public static class PassengerDTO {
        private String name;
        private String phone;

        public PassengerDTO() {}

        public PassengerDTO(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<PassengerDTO> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerDTO> passengers) {
        this.passengers = passengers;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCanceledBy() {
        return canceledBy;
    }

    public void setCanceledBy(String canceledBy) {
        this.canceledBy = canceledBy;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Boolean getPanicSent() {
        return panicSent;
    }

    public void setPanicSent(Boolean panicSent) {
        this.panicSent = panicSent;
    }

    public DriverRideHistoryResponseDTO(Integer id, List<PassengerDTO> passengers, String from, String to, String status, String canceledBy, String date, String startTime, String endTime, String price, String duration, String distance, Boolean panicSent) {
        this.id = id;
        this.passengers = passengers;
        this.from = from;
        this.to = to;
        this.status = status;
        this.canceledBy = canceledBy;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.duration = duration;
        this.distance = distance;
        this.panicSent = panicSent;
    }
    public DriverRideHistoryResponseDTO() {}
}
