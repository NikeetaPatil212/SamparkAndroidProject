package com.example.androidproject.model.template;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SettingsResponse {
    @SerializedName("isSuccess")   public boolean isSuccess;
    @SerializedName("message")     public String message;
    @SerializedName("settingsList") public List<SettingItem> settingsList;

    public static class SettingItem {
        @SerializedName("settingID")   public String settingID;
        @SerializedName("description") public String description;
        @SerializedName("value")       public String value;
    }
}