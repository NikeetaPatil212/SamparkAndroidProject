package com.example.androidproject.model.queue;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// SmsQueueRequest.java
public class SmsQueueRequest {
    @SerializedName("userID")      public int userID;
    @SerializedName("instituteID") public int instituteID;
    @SerializedName("smsList")     public List<SmsItem> smsList;

    public SmsQueueRequest(int userID, int instituteID, List<SmsItem> smsList) {
        this.userID      = userID;
        this.instituteID = instituteID;
        this.smsList     = smsList;
    }

    public static class SmsItem {
        @SerializedName("mobileNo")       public String mobileNo;
        @SerializedName("messageBody")    public String messageBody;
        @SerializedName("messageType")    public String messageType;
        @SerializedName("characterCount") public int    characterCount;
        @SerializedName("smsCreditUsed")  public int    smsCreditUsed;
        @SerializedName("category")       public String category;

        public SmsItem(String mobileNo, String messageBody, String category) {
            this.mobileNo       = mobileNo;
            this.messageBody    = messageBody;
            this.messageType    = "text";
            this.characterCount = messageBody.length();
            this.smsCreditUsed  = (int) Math.ceil(messageBody.length() / 160.0);
            this.category       = category;
        }
    }
}