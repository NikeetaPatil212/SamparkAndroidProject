package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class UpdateTransactionRequest {

    @SerializedName("utno")            public int    utno;
    @SerializedName("amount")          public double amount;
    @SerializedName("transactionDate") public String transactionDate;
    @SerializedName("comments")        public String comments;
    @SerializedName("operatorID")      public int    operatorID;
    @SerializedName("userID")          public int    userID;
    @SerializedName("instituteID")     public int    instituteID;

    public UpdateTransactionRequest(int utno, double amount, String transactionDate,
                                    String comments, int operatorID,
                                    int userID, int instituteID) {
        this.utno            = utno;
        this.amount          = amount;
        this.transactionDate = transactionDate;
        this.comments        = comments;
        this.operatorID      = operatorID;
        this.userID          = userID;
        this.instituteID     = instituteID;
    }
}