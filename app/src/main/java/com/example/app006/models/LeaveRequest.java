package com.example.app006.models;

import java.util.Date;

public class LeaveRequest {
    private String requestId; // Unique ID for the request
    private String employeeEmail;
    private String reason;
    private String startDate;
    private String endDate;
    private String status; // Pending, Approved, Rejected
    private Date timestamp; // <-- this is important


    public LeaveRequest() {
        // Default constructor required for Firebase or serialization
    }

    public LeaveRequest(String requestId, String employeeEmail, String reason,
                        String startDate, String endDate, String status, Date timestamp) {
        this.requestId = requestId;
        this.employeeEmail = employeeEmail;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
