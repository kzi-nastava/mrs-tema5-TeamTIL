package com.example.uberproject.dto.request;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {

    @SerializedName("senderEmail")
    private String senderEmail;

    @SerializedName("content")
    private String content;

    public SendMessageRequest(String senderEmail, String content) {
        this.senderEmail = senderEmail;
        this.content = content;
    }

    public String getSenderEmail() { return senderEmail; }
    public String getContent() { return content; }
}