package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InquiryReportResponse {
    @SerializedName("isSuccess")   public boolean              isSuccess;
    @SerializedName("message")     public String               message;
    @SerializedName("inquiryList") public List<InquiryReportItem> inquiryList;
}