package com.example.androidproject.model.certificate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CertificateResponse {

    @SerializedName("isSuccess")    public boolean isSuccess;
    @SerializedName("message")      public String  message;
    @SerializedName("studentList")  public List<CertificateStudent> studentList;
}
