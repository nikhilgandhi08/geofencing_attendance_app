package com.example.app006.admin.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app006.R;
import com.example.app006.models.AdminEmp;
import com.example.app006.models.Payroll;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PayrollFragment extends Fragment {

    private Spinner spinnerEmployee;
    private EditText editTextMonthYear, editTextBasicSalary, editTextAllowance, editTextDeduction, editTextBonus;
    private EditText editTextDaysPresent, editTextTotalWorkingDays;
    private TextView textNetSalary;
    private Button btnGeneratePayroll;

    private FirebaseFirestore db;
    private CollectionReference employeesCollection, payrollCollection;

    private List<AdminEmp> employeeList;
    private ArrayAdapter<String> employeeAdapter;
    private List<String> employeeNames;

    private String selectedEmployeeEmail = null;

    private String adminEmail;

    private static final String PREF_NAME = "MyPrefs";

    public PayrollFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_payroll, container, false);

        // Init views
        spinnerEmployee = view.findViewById(R.id.spinner_employee);
        editTextMonthYear = view.findViewById(R.id.editText_month_year);
        editTextBasicSalary = view.findViewById(R.id.editText_basic_salary);
        editTextAllowance = view.findViewById(R.id.editText_allowance);
        editTextDeduction = view.findViewById(R.id.editText_deduction);
        editTextBonus = view.findViewById(R.id.editText_bonus);

        editTextDaysPresent = view.findViewById(R.id.editText_days_present);
        editTextTotalWorkingDays = view.findViewById(R.id.editText_total_working_days);

        textNetSalary = view.findViewById(R.id.text_net_salary);
        btnGeneratePayroll = view.findViewById(R.id.btn_generate_payroll);

        // Setup Firestore
        db = FirebaseFirestore.getInstance();
        employeesCollection = db.collection("hr-employees");
        payrollCollection = db.collection("payroll");

        // Get admin email from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        adminEmail = prefs.getString("email", null);
        if (adminEmail == null) {
            Toast.makeText(getActivity(), "Admin email not found. Please login again.", Toast.LENGTH_LONG).show();
            return view;
        }

        employeeList = new ArrayList<>();
        employeeNames = new ArrayList<>();

        employeeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, employeeNames);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmployee.setAdapter(employeeAdapter);

        spinnerEmployee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position >= 0 && position < employeeList.size()) {
                    selectedEmployeeEmail = employeeList.get(position).getEmpEmail();
                } else {
                    selectedEmployeeEmail = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedEmployeeEmail = null;
            }
        });

        editTextMonthYear.setOnClickListener(v -> showMonthYearPicker());

        btnGeneratePayroll.setOnClickListener(v -> generatePayroll());

        fetchEmployees();

        return view;
    }

    private void fetchEmployees() {
        employeesCollection.whereEqualTo("adminEmail", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        employeeList.clear();
                        employeeNames.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            AdminEmp emp = doc.toObject(AdminEmp.class);
                            if (emp != null) {
                                employeeList.add(emp);
                                // Use only empEmail for spinner display
                                employeeNames.add(emp.getEmpEmail());
                            }
                        }
                        employeeAdapter.notifyDataSetChanged();

                        if (employeeNames.isEmpty()) {
                            Toast.makeText(getActivity(), "No employees found under this admin.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load employees: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showMonthYearPicker() {
        final Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String monthYear = getMonthName(selectedMonth) + " " + selectedYear;
                    editTextMonthYear.setText(monthYear);
                }, year, month, calendar.get(Calendar.DAY_OF_MONTH));

        // Hide day picker using reflection
        try {
            java.lang.reflect.Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    Object datePicker = datePickerDialogField.get(datePickerDialog);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialog.getClass().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        if ("mDaySpinner".equals(datePickerField.getName()) || "mDayPicker".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }

        datePickerDialog.show();
    }

    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        if (month >= 0 && month < 12) {
            return months[month];
        }
        return "";
    }

    private void generatePayroll() {
        if (selectedEmployeeEmail == null) {
            Toast.makeText(getActivity(), "Please select an employee", Toast.LENGTH_SHORT).show();
            return;
        }

        String monthYear = editTextMonthYear.getText().toString().trim();
        if (monthYear.isEmpty()) {
            Toast.makeText(getActivity(), "Please select month and year", Toast.LENGTH_SHORT).show();
            return;
        }

        String basicSalaryStr = editTextBasicSalary.getText().toString().trim();
        String allowanceStr = editTextAllowance.getText().toString().trim();
        String deductionStr = editTextDeduction.getText().toString().trim();
        String bonusStr = editTextBonus.getText().toString().trim();

        String daysPresentStr = editTextDaysPresent.getText().toString().trim();
        String totalWorkingDaysStr = editTextTotalWorkingDays.getText().toString().trim();

        if (basicSalaryStr.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter Basic Salary", Toast.LENGTH_SHORT).show();
            return;
        }
        if (daysPresentStr.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter Days Present", Toast.LENGTH_SHORT).show();
            return;
        }
        if (totalWorkingDaysStr.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter Total Working Days", Toast.LENGTH_SHORT).show();
            return;
        }

        double basicSalary = Double.parseDouble(basicSalaryStr);
        double allowance = allowanceStr.isEmpty() ? 0 : Double.parseDouble(allowanceStr);
        double deduction = deductionStr.isEmpty() ? 0 : Double.parseDouble(deductionStr);
        double bonus = bonusStr.isEmpty() ? 0 : Double.parseDouble(bonusStr);
        int daysPresent = Integer.parseInt(daysPresentStr);
        int totalWorkingDays = Integer.parseInt(totalWorkingDaysStr);

        double salaryProportion = (totalWorkingDays > 0) ? ((double) daysPresent / totalWorkingDays) : 1.0;
        double netSalary = (basicSalary + allowance + bonus - deduction) * salaryProportion;

        textNetSalary.setText("Net Pay: ₹ " + String.format("%.2f", netSalary));

        Payroll payroll = new Payroll(selectedEmployeeEmail, monthYear, basicSalary, allowance, deduction, bonus,
                daysPresent, totalWorkingDays, netSalary);

        payrollCollection.add(payroll)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Payroll generated successfully", Toast.LENGTH_SHORT).show();
                    clearInputs();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to generate payroll: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearInputs() {
        spinnerEmployee.setSelection(0);
        editTextMonthYear.setText("");
        editTextBasicSalary.setText("");
        editTextAllowance.setText("");
        editTextDeduction.setText("");
        editTextBonus.setText("");
        editTextDaysPresent.setText("");
        editTextTotalWorkingDays.setText("");
        textNetSalary.setText("Net Pay: ₹ -");
    }
}
