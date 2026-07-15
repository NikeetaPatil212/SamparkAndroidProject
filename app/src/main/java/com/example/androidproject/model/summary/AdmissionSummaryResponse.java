package com.example.androidproject.model.summary;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdmissionSummaryResponse {

    @SerializedName("isSuccess")   private boolean isSuccess;
    @SerializedName("message")     private String  message;
    @SerializedName("summaryList") private List<SummaryItem> summaryList;

    public boolean isSuccess()                { return isSuccess;   }
    public String  getMessage()               { return message;     }
    public List<SummaryItem> getSummaryList() { return summaryList; }

    public static class SummaryItem {
        @SerializedName("admissionDate") private String admissionDate;
        @SerializedName("courseName")    private String courseName;
        @SerializedName("finalFees")     private double finalFees;

        public String getAdmissionDate() { return admissionDate; }
        public String getCourseName()    { return courseName;    }
        public double getFinalFees()     { return finalFees;     }
    }
}