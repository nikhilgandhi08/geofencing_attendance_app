package com.example.app006.models;

import java.util.Objects;

public class AdminEmp {
    private String adminEmail;
    private String empEmail;



    // Default constructor (required for Firebase)
    public AdminEmp() {
    }

    public AdminEmp(String adminEmail, String empEmail) {
        this.adminEmail = adminEmail;
        this.empEmail = empEmail;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public void setEmpEmail(String empEmail) {
        this.empEmail = empEmail;
    }

    @Override
    public String toString() {
        return "AdminEmp{" +
                "adminEmail='" + adminEmail + '\'' +
                ", empEmail='" + empEmail + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminEmp adminEmp = (AdminEmp) o;
        return Objects.equals(adminEmail, adminEmp.adminEmail) &&
                Objects.equals(empEmail, adminEmp.empEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminEmail, empEmail);
    }
}
