package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;

public class AdmissionItem {

    @SerializedName("admissionID")       public int     admissionID;
    @SerializedName("admissionDate")     public String  admissionDate;
    @SerializedName("studentName")       public String  studentName;
    @SerializedName("mobile")            public String  mobile;
    @SerializedName("location")          public String  location;
    @SerializedName("courseID")          public int     courseID;
    @SerializedName("courseName")        public String  courseName;
    @SerializedName("batchID")           public int     batchID;
    @SerializedName("batchName")         public String  batchName;
    @SerializedName("timingID")          public Integer timingID;
    @SerializedName("timingDescription") public String  timingDescription;
    @SerializedName("fees")              public double  fees;
    @SerializedName("paid")              public double  paid;
    @SerializedName("outstanding")       public double  outstanding;
    @SerializedName("reminderDate")      public String  reminderDate;
    @SerializedName("gender")            public String  gender;

    // local-only selection state — not from API
    private transient boolean selected = false;
    public boolean isSelected()              { return selected; }
    public void    setSelected(boolean sel)  { selected = sel;  }
}
