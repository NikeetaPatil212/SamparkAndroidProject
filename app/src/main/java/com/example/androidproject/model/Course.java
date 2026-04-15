package com.example.androidproject.model;

public class Course {
    private int couseID;
    private String couse_Name;
    private String fees;
    private String scheme;
    private int certificate;
    private int duration;

    public Course(int couseID, String couse_Name, String fees, String scheme, int certificate, int duration) {
        this.couseID = couseID;
        this.couse_Name = couse_Name;
        this.fees = fees;
        this.scheme = scheme;
        this.certificate = certificate;
        this.duration = duration;
    }

    public int getCouseID() { return couseID; }
    public String getCouse_Name() { return couse_Name; }
    public String getFees() { return fees; }
    public String getScheme() { return scheme; }
    public int getCertificate() { return certificate; }
    public int getDuration() { return duration; }
}
