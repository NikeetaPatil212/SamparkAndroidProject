package com.example.androidproject.model.notification;

import com.google.gson.annotations.SerializedName;

// NotificationStudent.java
public class NotificationStudent {
    @SerializedName("adm_Id")             private int    admId;
    @SerializedName("firstName")          private String firstName;
    @SerializedName("lastName")           private String lastName;
    @SerializedName("fullName")           private String fullName;
    @SerializedName("mobile")             private String mobile;
    @SerializedName("courseID")           private int    courseID;
    @SerializedName("courseName")         private String courseName;
    @SerializedName("batchID")            private int    batchID;
    @SerializedName("batchName")          private String batchName;
    @SerializedName("timingID")           private int    timingID;
    @SerializedName("timingDescription")  private String timingDescription;

    public int    getAdmId()             { return admId; }
    public String getFirstName()         { return firstName; }
    public String getLastName()          { return lastName; }
    public String getFullName()          { return fullName; }
    public String getMobile()            { return mobile; }
    public int    getCourseID()          { return courseID; }
    public String getCourseName()        { return courseName; }
    public int    getBatchID()           { return batchID; }
    public String getBatchName()         { return batchName; }
    public int    getTimingID()          { return timingID; }
    public String getTimingDescription() { return timingDescription; }

    // For checkbox state in adapter
    private boolean selected = false;
    public boolean isSelected()              { return selected; }
    public void setSelected(boolean selected){ this.selected = selected; }



}
