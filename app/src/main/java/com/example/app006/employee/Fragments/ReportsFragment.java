package com.example.app006.employee.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app006.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private TextView tvEmployeeName, tvDesignationDepartment, tvReportPeriod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.employee_fragment_reports, container, false);

        // 1️⃣ Initialize views
        tvEmployeeName = view.findViewById(R.id.tv_employee_name);
        tvDesignationDepartment = view.findViewById(R.id.tv_designation_department);
        tvReportPeriod = view.findViewById(R.id.tv_report_period);

        // 2️⃣ Load from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        String role = sharedPreferences.getString("role", null);

        if (name == null || role == null) {
            Toast.makeText(requireContext(), "Unable to load user details", Toast.LENGTH_SHORT).show();
            Log.d("ReportsFragment", "SharedPreferences missing: name=" + name + " role=" + role);
        } else {
            tvEmployeeName.setText(name);
            tvDesignationDepartment.setText(role);
        }

        // 3️⃣ Display this month’s report period
        setReportPeriod();

        return view;
    }

    private void setReportPeriod() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        String start = sdf.format(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String end = sdf.format(cal.getTime());

        tvReportPeriod.setText(start + " - " + end);
    }
}
