package com.example.androidproject.model.course;

import com.google.gson.annotations.SerializedName;

public class BatchTimeAddResponse {

    @SerializedName(value = "isSuccess", alternate = {"IsSuccess"})
    public boolean isSuccess;

    @SerializedName(value = "message", alternate = {"Message"})
    public String message;
}
