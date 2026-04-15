package com.example.androidproject.model;

import java.util.List;

public class BatchResponse {

    private String message;
    private List<Batch> batchlist;

    public String getMessage() {
        return message;
    }

    public List<Batch> getBatchList() {
        return batchlist;
    }
}

