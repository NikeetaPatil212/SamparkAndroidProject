package com.example.androidproject.model.certificate;

import com.google.gson.annotations.SerializedName;

public class CertificateStudent {
    @SerializedName("admissionID")   public int     admissionID;
    @SerializedName("admissionDate") public String  admissionDate;
    @SerializedName("studentID")     public int     studentID;
    @SerializedName("studentName")   public String  studentName;
    @SerializedName("mobile")        public String  mobile;
    @SerializedName("location")      public String  location;
    @SerializedName("courseID")      public int     courseID;
    @SerializedName("course")        public String  course;
    @SerializedName("scheme")        public String  scheme;
    @SerializedName("batch")         public String  batch;
    @SerializedName("fee")           public double  fee;
    @SerializedName("paid")          public double  paid;
    @SerializedName("outstanding")   public double  outstanding;
    @SerializedName("status")        public String  status;
    @SerializedName("certificate")   public boolean certificate;
    @SerializedName("certificateID") public String  certificateID;
    @SerializedName("issuer")        public String  issuer;
    @SerializedName("issueDate")     public String  issueDate;
    @SerializedName("result")        public String  result;
    @SerializedName("percentage")    public Double  percentage;   // Double (nullable)
    @SerializedName("issuerContact") public String  issuerContact;
    @SerializedName("issuerImg")     public String  issuerImg;
}