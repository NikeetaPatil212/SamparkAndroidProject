package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class InstituteModel {

    @SerializedName("instituteID")
    private String id;

    @SerializedName("instituteName")
    private String name;

    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name; // Spinner text
    }
}
