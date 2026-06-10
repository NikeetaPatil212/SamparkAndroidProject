package com.example.androidproject.model.template;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TemplateResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("templateList")
    private List<TemplateModel> templateList;

    public boolean isSuccess()                   { return isSuccess; }
    public String getMessage()                   { return message; }
    public List<TemplateModel> getTemplateList() { return templateList; }

    public static class TemplateModel {
        @SerializedName("templateID")   public int     templateID;
        @SerializedName("category")     public String  category;
        @SerializedName("accessToken")  public String  accessToken;
        @SerializedName("instanceID")   public String  instanceID;
        @SerializedName("wa_MR")        public String  wa_MR;
        @SerializedName("wa_HI")        public String  wa_HI;
        @SerializedName("wa_EN")        public String  wa_EN;
        @SerializedName("sms_MR")       public String  sms_MR;
        @SerializedName("sms_HI")       public String  sms_HI;
        @SerializedName("sms_EN")       public String  sms_EN;
        @SerializedName("userID")       public int     userID;
        @SerializedName("instituteID")  public int     instituteID;
        @SerializedName("isActive")     public boolean isActive;
    }
}