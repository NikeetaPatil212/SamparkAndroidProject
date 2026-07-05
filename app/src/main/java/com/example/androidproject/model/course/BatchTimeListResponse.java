package com.example.androidproject.model.course;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BatchTimeListResponse {

    @SerializedName(value = "isSuccess", alternate = {"IsSuccess"})
    public boolean isSuccess;

    @SerializedName(value = "message", alternate = {"Message"})
    public String message;

    @SerializedName(value = "batchList", alternate = {"batchlist", "BatchList"})
    public List<BatchTimeItem> batchList;
}
