package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class PaymentHistory {

    @SerializedName("trDate")
    private String trDate;

    @SerializedName("amount")
    private String amount;

    @SerializedName("receiptNo")
    private String receiptNo;

    // ✅ Getters
    public String getTrDate() {
        return trDate;
    }

    public String getAmount() {
        return amount;
    }

    public String getReceiptNo() {
        return receiptNo;
    }
}