package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BatchResponse {

    @SerializedName("message")
    private String message;

    // Try "batchList" — if still null try "BatchList"
    @SerializedName("batchlist")
    private List<Batch> batchlist;

    public String getMessage()        { return message; }
    public List<Batch> getBatchList() { return batchlist; }
}