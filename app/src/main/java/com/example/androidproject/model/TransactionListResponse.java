package com.example.androidproject.model;


import com.example.androidproject.model.template.StudentInfo;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionListResponse {

    @SerializedName("isSuccess")
    public boolean isSuccess;

    @SerializedName("message")
    public String message;

    @SerializedName("studentInfo")
    public StudentInfo studentInfo;

    @SerializedName("transactionHistory")
    public List<TransactionItem> transactionHistory;
}