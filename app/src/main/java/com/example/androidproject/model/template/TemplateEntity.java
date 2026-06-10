package com.example.androidproject.model.template;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "templates")
public class TemplateEntity {

    @PrimaryKey
    public int    templateID;
    public String category;
    public String accessToken;
    public String instanceID;

    public String wa_MR;
    public String wa_HI;
    public String wa_EN;

    public String sms_MR;
    public String sms_HI;
    public String sms_EN;

    public int     userID;
    public int     instituteID;
    public boolean isActive;

    public long    cachedAt;   // epoch ms — used for 24hr expiry check
}