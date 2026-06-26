package com.example.androidproject.model.certificate;

import com.google.gson.annotations.SerializedName;

// UpdateCertificateRequest.java
public class UpdateCertificateRequest {
    @SerializedName("admissionID")   public int    admissionID;
    @SerializedName("certificateID") public String certificateID;
    @SerializedName("issuer")        public String issuer;
    @SerializedName("issueDate")     public String issueDate;
    @SerializedName("result")        public String result;
    @SerializedName("percentage")    public double percentage;
    @SerializedName("issuerContact") public String issuerContact;
    @SerializedName("issuerImg")     public String issuerImg;
    @SerializedName("userID")        public int    userID;
    @SerializedName("instituteID")   public int    instituteID;
    @SerializedName("operatorID")    public int    operatorID;

    public UpdateCertificateRequest(int admissionID, String certificateID, String issuer,
                                    String issueDate, String result, double percentage,
                                    String issuerContact, String issuerImg,
                                    int userID, int instituteID, int operatorID) {
        this.admissionID   = admissionID;
        this.certificateID = certificateID;
        this.issuer        = issuer;
        this.issueDate     = issueDate;
        this.result        = result;
        this.percentage    = percentage;
        this.issuerContact = issuerContact;
        this.issuerImg     = issuerImg;
        this.userID        = userID;
        this.instituteID   = instituteID;
        this.operatorID    = operatorID;
    }
}

