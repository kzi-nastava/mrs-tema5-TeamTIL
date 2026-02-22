package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class LocationResponseDTO {
    @SerializedName("name")
    private String name;
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("longitude")
    private String longitude;
}
