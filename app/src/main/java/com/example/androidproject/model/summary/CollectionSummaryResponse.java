package com.example.androidproject.model.summary;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CollectionSummaryResponse {

    @SerializedName("isSuccess")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("summaryList")
    private List<SummaryItem> summaryList;

    public boolean isSuccess()                { return success; }
    public String getMessage()                { return message; }
    public List<SummaryItem> getSummaryList() { return summaryList; }

    public static class SummaryItem {

        @SerializedName("receiptDate")
        private String receiptDate;

        @SerializedName("receiptCount")
        private int receiptCount;

        @SerializedName("totalAmount")
        private double totalAmount;

        public String getReceiptDate()  { return receiptDate; }
        public int getReceiptCount()    { return receiptCount; }
        public double getTotalAmount()  { return totalAmount; }
    }
}