package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class TransactionItem {

    @SerializedName("utno")
    public int utno;

    @SerializedName("trno")
    public int trno;

    @SerializedName("transactionDate")
    public String transactionDate;

    @SerializedName("transactionType")
    public String transactionType;

    @SerializedName("debit")
    public double debit;

    @SerializedName("credit")
    public double credit;
}