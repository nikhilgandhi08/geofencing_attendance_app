package com.example.app006.employee.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app006.R;
import com.example.app006.adapters.PayrollAdapter;
import com.example.app006.models.Payroll;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollFragment extends Fragment {

    private static final String PREF_NAME = "MyPrefs";

    private RecyclerView recyclerViewPayroll;
    private PayrollAdapter payrollAdapter;
    private List<Payroll> payrollList;
    private TextView textEmpty;

    private FirebaseFirestore db;
    private String employeeEmail;

    public EmployeePayrollFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.employee_fragment_payroll, container, false);

        recyclerViewPayroll = view.findViewById(R.id.recyclerView_payrollHistory);
        textEmpty = view.findViewById(R.id.textView_empty);

        recyclerViewPayroll.setLayoutManager(new LinearLayoutManager(requireContext()));

        payrollList = new ArrayList<>();
        payrollAdapter = new PayrollAdapter(payrollList);
        recyclerViewPayroll.setAdapter(payrollAdapter);

        db = FirebaseFirestore.getInstance();

        // Get logged in employee's email from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        employeeEmail = prefs.getString("email", null);

        if (employeeEmail == null) {
            Toast.makeText(getActivity(), "User email not found. Please login again.", Toast.LENGTH_LONG).show();
        } else {
            fetchPayrollHistory();
        }

        return view;
    }

    private void fetchPayrollHistory() {
        textEmpty.setVisibility(View.GONE);

        db.collection("payroll")
                .whereEqualTo("email", employeeEmail)
                //.orderBy("monthYear")  // comment out if causing errors
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    payrollList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Payroll payroll = doc.toObject(Payroll.class);
                            payrollList.add(payroll);
                        }
                    }
                    payrollAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load payroll history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}
