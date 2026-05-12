package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BatchTimingResponse {

    @SerializedName("isSuccess")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("batchList")
    private List<BatchTimingItem> batchList;

    public boolean isSuccess()              { return success;   }
    public String getMessage()              { return message;   }
    public List<BatchTimingItem> getBatchList() { return batchList; }

    // ── Inner model ────────────────────────────────────────────────────────
    public static class BatchTimingItem {

        @SerializedName("timingID")
        private int timingID;

        @SerializedName("timing_Description")
        private String timingDescription;

        @SerializedName("startTime")
        private String startTime;

        @SerializedName("endTime")
        private String endTime;

        @SerializedName("capacity")
        private int capacity;

        @SerializedName("availableSeats")
        private int availableSeats;

        @SerializedName("filled")
        private int filled;

        public int    getTimingID()          { return timingID;          }
        public String getTimingDescription() { return timingDescription; }
        public String getStartTime()         { return startTime;         }
        public String getEndTime()           { return endTime;           }
        public int    getCapacity()          { return capacity;          }
        public int    getAvailableSeats()    { return availableSeats;    }
        public int    getFilled()            { return filled;            }

        /** Label shown inside the dialog dropdown */
        public String dropdownLabel() {
            return timingDescription + "  (" + startTime + " – " + endTime + ")";
        }
    }
}