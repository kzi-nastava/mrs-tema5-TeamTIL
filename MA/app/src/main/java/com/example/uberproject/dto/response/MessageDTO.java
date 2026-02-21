package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class MessageDTO {

    @SerializedName("id")
    private Integer id;

    @SerializedName("content")
    private String content;

    @SerializedName("timestamp")
    private String timestamp; // HH:mm

    @SerializedName("date")
    private String date; // yyyy-MM-dd

    @SerializedName("userType")
    private String userType; // ADMINISTRATOR | REGISTERED_USER | DRIVER

    @SerializedName("chatId")
    private Integer chatId;

    public Integer getId() { return id; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public String getDate() { return date; }
    public String getUserType() { return userType; }
    public Integer getChatId() { return chatId; }

    public boolean isFromAdmin() {
        return "ADMINISTRATOR".equals(userType);
    }
}