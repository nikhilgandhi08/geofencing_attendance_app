package com.example.app006.models;

import java.util.Objects;


public class Employee {
    private String email;
    private String name;
    private String role;
    private String bankAccount;
    private double basicSalary;
    private double hra;
    private double allowances;
    private double deductions;  // tax, PF etc
    private int daysWorked;
    private int leavesTaken;

    // getters/setters + constructor

    public double getGrossSalary() {
        return basicSalary + hra + allowances;
    }

    public double getNetSalary() {
        return getGrossSalary() - deductions;
    }
}
