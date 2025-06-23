package com.example.app006.models;

public class LeaveRequestNew {
    private String employeeName;
    private String reason;
    private String startDate;
    private String endDate;
    private String status;

    public LeaveRequestNew() { }

    public LeaveRequestNew(String employeeName, String reason, String startDate, String endDate, String status) {
        this.employeeName = employeeName;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
