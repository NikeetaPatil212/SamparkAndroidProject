package com.example.androidproject.model.queue;

import com.google.gson.annotations.SerializedName;

// WhatsAppQueueResponse.java
public class WhatsAppQueueResponse {
    @SerializedName("isSuccess")     public boolean isSuccess;
    @SerializedName("message")       public String  message;
    @SerializedName("insertedCount") public int     insertedCount;
}