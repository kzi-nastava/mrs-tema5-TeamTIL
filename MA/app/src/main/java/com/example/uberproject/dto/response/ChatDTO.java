package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ChatDTO {

    @SerializedName("id")
    private Integer id;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("userFirstName")
    private String userFirstName;

    @SerializedName("userLastName")
    private String userLastName;

    @SerializedName("userType")
    private String userType; // REGISTERED_USER | DRIVER

    @SerializedName("messages")
    private List<MessageDTO> messages = new ArrayList<>();

    @SerializedName("lastMessageContent")
    private String lastMessageContent;

    @SerializedName("lastMessageTime")
    private String lastMessageTime;

    @SerializedName("lastMessageUserType")
    private String lastMessageUserType;

    public Integer getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getUserFirstName() { return userFirstName; }
    public String getUserLastName() { return userLastName; }
    public String getUserType() { return userType; }
    public List<MessageDTO> getMessages() { return messages != null ? messages : new ArrayList<>(); }
    public String getLastMessageContent() { return lastMessageContent; }
    public String getLastMessageTime() { return lastMessageTime; }
    public String getLastMessageUserType() { return lastMessageUserType; }

    public void setMessages(List<MessageDTO> messages) { this.messages = messages; }
    public void setLastMessageContent(String c) { this.lastMessageContent = c; }
    public void setLastMessageTime(String t) { this.lastMessageTime = t; }
    public void setLastMessageUserType(String u) { this.lastMessageUserType = u; }

    public String getUserFullName() {
        return ((userFirstName != null ? userFirstName : "") + " "
                + (userLastName != null ? userLastName : "")).trim();
    }

    public String getRoleLabel() {
        if (userType == null) return "";
        switch (userType) {
            case "DRIVER": return "driver";
            case "ADMINISTRATOR": return "admin";
            default: return "user";
        }
    }
}