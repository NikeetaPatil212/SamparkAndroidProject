package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class UpdateTransactionResponse {
    @SerializedName("isSuccess") public boolean isSuccess;
    @SerializedName("message")   public String  message;
}