package com.example.app006.models;

public class Payroll {
    private String email;
    private String monthYear;
    private double basicSalary;
    private double allowance;
    private double deduction;
    private double bonus;
    private int daysPresent;
    private int totalWorkingDays;
    private double netSalary;

    public Payroll() {
        // Default constructor
    }

    public Payroll(String email, String monthYear, double basicSalary, double allowance,
                   double deduction, double bonus, int daysPresent, int totalWorkingDays, double netSalary) {
        this.email = email;
        this.monthYear = monthYear;
        this.basicSalary = basicSalary;
        this.allowance = allowance;
        this.deduction = deduction;
        this.bonus = bonus;
        this.daysPresent = daysPresent;
        this.totalWorkingDays = totalWorkingDays;
        this.netSalary = netSalary;
    }

    // Getters and Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public double getAllowance() {
        return allowance;
    }

    public void setAllowance(double allowance) {
        this.allowance = allowance;
    }

    public double getDeduction() {
        return deduction;
    }

    public void setDeduction(double deduction) {
        this.deduction = deduction;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public int getDaysPresent() {
        return daysPresent;
    }

    public void setDaysPresent(int daysPresent) {
        this.daysPresent = daysPresent;
    }

    public int getTotalWorkingDays() {
        return totalWorkingDays;
    }

    public void setTotalWorkingDays(int totalWorkingDays) {
        this.totalWorkingDays = totalWorkingDays;
    }

    public double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }
}
