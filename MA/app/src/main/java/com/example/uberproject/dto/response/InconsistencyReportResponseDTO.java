package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class InconsistencyReportResponseDTO {

    @SerializedName("id")
    private Integer id;

    @SerializedName("message")
    private String message;

    public Integer getId() { return id; }
    public String getMessage() { return message; }
}