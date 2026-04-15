package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("userID")
    private String userID;

    @SerializedName("data_List")
    private List<InstituteModel> dataList;

    public boolean isSuccess() { return isSuccess; }
    public String getMessage() { return message; }
    public String getUserID() { return userID; }
    public List<InstituteModel> getDataList() { return dataList; }
}
