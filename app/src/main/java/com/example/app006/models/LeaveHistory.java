package com.example.app006.models;

import java.util.Date;

public class LeaveHistory {
    private String reason;
    private String startDate;
    private String endDate;
    private String status;



    public LeaveHistory() {} // Needed for Firestore

    public LeaveHistory(String reason, String startDate, String endDate, String status) {
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public String getReason() { return reason; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }


}
