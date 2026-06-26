package com.example.androidproject.model.certificate;

import com.google.gson.annotations.SerializedName;

// UploadImageResponse.java
public class UploadImageResponse {
    @SerializedName("isSuccess") public boolean isSuccess;
    @SerializedName("message")   public String  message;
    @SerializedName("imageUrl")  public String  imageUrl; // adjust field name per actual response
}