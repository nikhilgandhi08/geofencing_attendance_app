package com.example.app006.admin.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.adapters.EmployeeAdapter;
import com.example.app006.models.AdminEmp;
import com.example.app006.models.HelperClass; // Assuming HelperClass is the model for users table
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.List;

public class EmployeeManagementFragment extends Fragment {

    private EditText etEmpEmail;
    private Button btnAddEmployee, btnDeleteEmployee;
    private RecyclerView rvEmployeeList;
    private FirebaseFirestore db;
    private CollectionReference employeesCollection;
    private CollectionReference usersCollection;
    private String adminEmail;
    private EmployeeAdapter employeeAdapter;
    private List<AdminEmp> employeeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_employeemgmt_fragment, container, false);

        etEmpEmail = view.findViewById(R.id.et_emp_email);
        btnAddEmployee = view.findViewById(R.id.btn_add_employee);
        btnDeleteEmployee = view.findViewById(R.id.btn_delete_employee);
        rvEmployeeList = view.findViewById(R.id.rv_employee_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        employeesCollection = db.collection("hr-employees"); // Firestore collection name for employees
        usersCollection = db.collection("users"); // Firestore collection name for users

        // Initialize employee list and adapter
        employeeList = new ArrayList<>();
        employeeAdapter = new EmployeeAdapter(employeeList);
        rvEmployeeList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvEmployeeList.setAdapter(employeeAdapter);

        // Retrieve admin email from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        adminEmail = sharedPreferences.getString("email", "default@example.com");

        // Set listeners for buttons
        btnAddEmployee.setOnClickListener(v -> addEmployee());
        btnDeleteEmployee.setOnClickListener(v -> deleteEmployee());

        // Fetch existing employees from Firestore
        fetchEmployees();

        return view;
    }

    // Add employee to Firestore
    private void addEmployee() {
        String empEmail = etEmpEmail.getText().toString().trim();

        // Validate if the email is empty
        if (empEmail.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter an employee email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(empEmail).matches()) {
            Toast.makeText(getActivity(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if employee exists in the 'users' collection first
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", empEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Employee exists in the 'users' collection
                        // Now check if the employee exists in the 'hr-employees' collection
                        checkEmployeeExistsInHrEmployees(empEmail);
                    } else {
                        // Employee does not exist in the 'users' collection
                        Toast.makeText(getActivity(), "This email is not registered in the system.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error checking if employee exists in users", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to check if the employee already exists in 'hr-employees' collection
    private void checkEmployeeExistsInHrEmployees(String empEmail) {
        employeesCollection.whereEqualTo("empEmail", empEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Employee exists in the 'hr-employees' collection under some admin
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String existingAdminEmail = documentSnapshot.getString("adminEmail");

                        // Show toast with admin email
                        Toast.makeText(getActivity(),
                                "Employee already exists under admin: " + existingAdminEmail,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Employee does not exist in 'hr-employees', proceed with adding the employee
                        addEmployeeToHrEmployees(empEmail);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error checking employee existence in hr-employees", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to add employee to 'hr-employees' collection
    private void addEmployeeToHrEmployees(String empEmail) {
        // Create an AdminEmp object
        AdminEmp adminEmp = new AdminEmp(adminEmail, empEmail);

        // Add employee to Firestore
        employeesCollection.add(adminEmp)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Employee added successfully", Toast.LENGTH_SHORT).show();
                    etEmpEmail.setText(""); // Clear input field
                    fetchEmployees(); // Update the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to add employee", Toast.LENGTH_SHORT).show();
                });
    }

    // Delete employee from Firestore
    private void deleteEmployee() {
        String empEmail = etEmpEmail.getText().toString().trim();

        if (empEmail.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter an employee email to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the employee exists under the current admin
        employeesCollection.whereEqualTo("empEmail", empEmail)
                .whereEqualTo("adminEmail", adminEmail)  // Check if the employee is under the current admin
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Employee found under current admin, proceed to delete
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            documentSnapshot.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getActivity(), "Employee deleted successfully", Toast.LENGTH_SHORT).show();
                                        etEmpEmail.setText(""); // Clear input field
                                        fetchEmployees(); // Update the list
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Failed to delete employee", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // No employee found under the current admin
                        Toast.makeText(getActivity(), "This employee does not exist or is under another admin.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error finding employee to delete", Toast.LENGTH_SHORT).show();
                });
    }

    // Fetch employee list from Firestore for the current logged-in admin
    private void fetchEmployees() {
        employeesCollection.whereEqualTo("adminEmail", adminEmail) // Filter by adminEmail
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        employeeList.clear(); // Clear the existing list
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            AdminEmp adminEmp = documentSnapshot.toObject(AdminEmp.class);
                            if (adminEmp != null) {
                                employeeList.add(adminEmp); // Add filtered employee
                            }
                        }
                        employeeAdapter.notifyDataSetChanged(); // Notify adapter about the change
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load employee list", Toast.LENGTH_SHORT).show();
                });
    }
}
