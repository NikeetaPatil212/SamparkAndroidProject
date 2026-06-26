package com.example.androidproject.model.queue;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// WhatsAppQueueRequest.java
public class WhatsAppQueueRequest {
    @SerializedName("userID")      public int userID;
    @SerializedName("instituteID") public int instituteID;
    @SerializedName("whatsappList") public List<WhatsAppItem> whatsappList;

    public WhatsAppQueueRequest(int userID, int instituteID, List<WhatsAppItem> list) {
        this.userID      = userID;
        this.instituteID = instituteID;
        this.whatsappList = list;
    }

    public static class WhatsAppItem {
        @SerializedName("mobileNo")    public String mobileNo;
        @SerializedName("messageBody") public String messageBody;
        @SerializedName("category")    public String category;
        @SerializedName("accessToken") public String accessToken;
        @SerializedName("instanceID")  public String instanceID;

        public WhatsAppItem(String mobileNo, String messageBody,
                            String category, String accessToken, String instanceID) {
            this.mobileNo    = mobileNo;
            this.messageBody = messageBody;
            this.category    = category;
            this.accessToken = accessToken;
            this.instanceID  = instanceID;
        }
    }
}