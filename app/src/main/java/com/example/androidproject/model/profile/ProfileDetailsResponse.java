package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProfileDetailsResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("transactions")
    private List<Transaction> transactions;

    @SerializedName("admissions")
    private List<Admission> admissions;

    @SerializedName("profile")
    private Profile profile;

    @SerializedName("details_of_admission")
    private DetailsOfAdmission detailsOfAdmission;

    // ── Getters ───────────────────────────────────────────────────
    public boolean isSuccess()                    { return isSuccess; }
    public String getMessage()                    { return message; }
    public List<Transaction> getTransactions()    { return transactions; }
    public List<Admission> getAdmissions()        { return admissions; }
    public Profile getProfile()                   { return profile; }
    public DetailsOfAdmission getDetailsOfAdmission() { return detailsOfAdmission; }

    // ══ Transaction ═══════════════════════════════════════════════
    public static class Transaction {

        @SerializedName("trno")
        private int trno;

        @SerializedName("trType")
        private String trType;

        @SerializedName("admissionFee")
        private int admissionFee;

        @SerializedName("receipt")
        private int receipt;

        @SerializedName("receiptNo")
        private String receiptNo;

        @SerializedName("description")
        private String description;

        @SerializedName("operaterID")
        private int operaterID;

        public int getTrno()          { return trno; }
        public String getTrType()     { return trType; }
        public int getAdmissionFee()  { return admissionFee; }
        public int getReceipt()       { return receipt; }
        public String getReceiptNo()  { return receiptNo; }
        public String getDescription(){ return description; }
        public int getOperaterID()    { return operaterID; }
    }

    // ══ Admission ═════════════════════════════════════════════════
    public static class Admission {

        @SerializedName("admissionID")
        private int admissionID;

        @SerializedName("admissionDate")
        private String admissionDate;

        @SerializedName("course")
        private String course;

        @SerializedName("batch")
        private String batch;

        @SerializedName("fees")
        private String fees;

        @SerializedName("paid")
        private String paid;

        @SerializedName("outstanding")
        private String outstanding;

        public int getAdmissionID()     { return admissionID; }
        public String getAdmissionDate(){ return admissionDate; }
        public String getCourse()       { return course; }
        public String getBatch()        { return batch; }
        public String getFees()         { return fees; }
        public String getPaid()         { return paid; }
        public String getOutstanding()  { return outstanding; }
    }

    // ══ Profile ═══════════════════════════════════════════════════
    public static class Profile {

        @SerializedName("full_Name")
        private String full_Name;

        @SerializedName("mobile")
        private String mobile;

        @SerializedName("alternateNo")
        private String alternateNo;

        @SerializedName("email")
        private String email;

        @SerializedName("address")
        private String address;

        @SerializedName("dob")
        private String dob;

        @SerializedName("gender")
        private String gender;

        @SerializedName("imgurl")
        private String imgurl;

        @SerializedName("school")
        private String school;

        public String getFull_Name()   { return full_Name; }
        public String getMobile()      { return mobile; }
        public String getAlternateNo() { return alternateNo; }
        public String getEmail()       { return email; }
        public String getAddress()     { return address; }
        public String getDob()         { return dob; }
        public String getGender()      { return gender; }
        public String getImgurl()      { return imgurl; }
        public String getSchool()      { return school; }
    }

    // ══ DetailsOfAdmission ════════════════════════════════════════
    public static class DetailsOfAdmission {

        @SerializedName("admissionID")
        private int admissionID;

        @SerializedName("admissionDate")
        private String admissionDate;

        @SerializedName("courseID")
        private int courseID;

        @SerializedName("course")
        private String course;

        @SerializedName("batchID")
        private int batchID;

        @SerializedName("batch")
        private String batch;

        @SerializedName("fees")
        private int fees;

        @SerializedName("paid")
        private int paid;

        @SerializedName("outstanding")
        private int outstanding;

        public int getAdmissionID()     { return admissionID; }
        public String getAdmissionDate(){ return admissionDate; }
        public int getCourseID()        { return courseID; }
        public String getCourse()       { return course; }
        public int getBatchID()         { return batchID; }
        public String getBatch()        { return batch; }
        public int getFees()            { return fees; }
        public int getPaid()            { return paid; }
        public int getOutstanding()     { return outstanding; }
    }
}