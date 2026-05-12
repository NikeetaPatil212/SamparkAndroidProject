package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudyMaterialUpdateRequest {

    @SerializedName("admissionIDs")  private List<Integer> admissionIDs;
    @SerializedName("userID")        private int userID;
    @SerializedName("instituteID")   private int instituteID;
    @SerializedName("materialDate")  private String materialDate;

    public StudyMaterialUpdateRequest(List<Integer> admissionIDs,
                                      int userID, int instituteID,
                                      String materialDate) {
        this.admissionIDs = admissionIDs;
        this.userID       = userID;
        this.instituteID  = instituteID;
        this.materialDate = materialDate;
    }
}