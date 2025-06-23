package com.example.app006.models;

import java.util.Date;

public class TestData {
    private String adminEmail;
    private String employeeEmail;
    private String testTakerType;
    private Date issuedDate;
    private Date dueDate;
    private String status;
    private int totalScore;
    private int scoreObtained;

    public TestData() { }

    public TestData(String adminEmail, String employeeEmail,
                    String testTakerType, Date issuedDate,
                    Date dueDate, String status,
                    int totalScore, int scoreObtained) {
        this.adminEmail    = adminEmail;
        this.employeeEmail = employeeEmail;
        this.testTakerType = testTakerType;
        this.issuedDate    = issuedDate;
        this.dueDate       = dueDate;
        this.status        = status;
        this.totalScore    = totalScore;
        this.scoreObtained = scoreObtained;
    }

    // getters & setters omitted for brevity
}
