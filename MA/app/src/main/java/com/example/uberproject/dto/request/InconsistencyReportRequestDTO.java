package com.example.uberproject.dto.request;

import com.google.gson.annotations.SerializedName;

public class InconsistencyReportRequestDTO {

    @SerializedName("passengerEmail")
    private String passengerEmail;

    @SerializedName("description")
    private String description;

    @SerializedName("attachmentBase64")
    private String attachmentBase64;

    public InconsistencyReportRequestDTO(String passengerEmail, String description, String attachmentBase64) {
        this.passengerEmail = passengerEmail;
        this.description = description;
        this.attachmentBase64 = attachmentBase64;
    }

    public String getPassengerEmail() { return passengerEmail; }
    public String getDescription() { return description; }
    public String getAttachmentBase64() { return attachmentBase64; }
}