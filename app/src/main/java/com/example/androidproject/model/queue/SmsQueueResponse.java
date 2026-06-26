package com.example.androidproject.model.queue;

import com.google.gson.annotations.SerializedName;

// SmsQueueResponse.java
public class SmsQueueResponse {
    @SerializedName("isSuccess")     public boolean isSuccess;
    @SerializedName("message")       public String  message;
    @SerializedName("insertedCount") public int     insertedCount;
}