package com.example.app006.employee.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.adapters.LeaveHistoryAdapter;
import com.example.app006.models.LeaveHistory;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeaveManagementFragment extends Fragment {

    private EditText leaveReason;
    private Button startDateButton, endDateButton, applyLeaveButton;
    private TextView leaveStatus;
    private String startDate, endDate;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String employeeEmail;
    private RecyclerView rvLeaveHistory;
    private LeaveHistoryAdapter adapter;
    private List<LeaveHistory> leaveList = new ArrayList<>();
    private List<LeaveHistory> visibleLeaveList = new ArrayList<>();
    private final SimpleDateFormat firestoreDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private Date selectedStartDate, selectedEndDate;
    private boolean isExpanded = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.employee_fragment_leave, container, false);

        leaveReason = view.findViewById(R.id.leave_reason);
        startDateButton = view.findViewById(R.id.start_date_button);
        endDateButton = view.findViewById(R.id.end_date_button);
        applyLeaveButton = view.findViewById(R.id.apply_leave_button);
        leaveStatus = view.findViewById(R.id.leave_status);
        rvLeaveHistory = view.findViewById(R.id.rv_leave_history);
        rvLeaveHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaveHistoryAdapter(visibleLeaveList);
        rvLeaveHistory.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("LeavePrefs", Context.MODE_PRIVATE);

        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        employeeEmail = loginPrefs.getString("email", "");

        if (employeeEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: No employee email found.", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadLeaveData();
        fetchCurrentLeaveStatus();
        loadLeaveHistory();

        startDateButton.setOnClickListener(v -> openDatePicker(true));
        endDateButton.setOnClickListener(v -> openDatePicker(false));
        applyLeaveButton.setOnClickListener(v -> applyLeave());

        MaterialButton viewAllButton = view.findViewById(R.id.view_all_leaves_button);
        viewAllButton.setOnClickListener(v -> {
            if (!isExpanded) {
                updateVisibleList(10);
                isExpanded = true;
                viewAllButton.setText("Showing Last 10 Leaves");
            } else {
                updateVisibleList(3);
                isExpanded = false;
                viewAllButton.setText("View All Leave History");
            }
        });


        return view;
    }

    private void openDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = firestoreDateFormat.format(selectedCal.getTime());

                    if (isStartDate) {
                        selectedStartDate = selectedCal.getTime();
                        startDate = formattedDate;
                        startDateButton.setText("Start Date: " + startDate);
                    } else {
                        selectedEndDate = selectedCal.getTime();
                        endDate = formattedDate;
                        endDateButton.setText("End Date: " + endDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void applyLeave() {
        String reason = leaveReason.getText().toString().trim();
        if (reason.isEmpty() || startDate == null || endDate == null) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        applyLeaveButton.setEnabled(false);

        Map<String, Object> leaveRequest = new HashMap<>();
        leaveRequest.put("employeeEmail", employeeEmail);
        leaveRequest.put("reason", reason);
        leaveRequest.put("startDate", startDate);
        leaveRequest.put("endDate", endDate);
        leaveRequest.put("status", "Pending");

        db.collection("leave-requests")
                .add(leaveRequest)
                .addOnSuccessListener(documentReference -> {
                    saveLeaveData(reason);
                    leaveStatus.setText("Leave Status: Pending");
                    Toast.makeText(getContext(), "Leave Applied Successfully!", Toast.LENGTH_SHORT).show();
                    applyLeaveButton.setEnabled(true);

                    leaveReason.setText("");
                    startDateButton.setText("Start Date");
                    endDateButton.setText("End Date");
                    startDate = endDate = null;
                })
                .addOnFailureListener(e -> {
                    Log.e("LeaveManagement", "Error submitting leave: " + e.getMessage());
                    Toast.makeText(getContext(), "Error submitting leave!", Toast.LENGTH_SHORT).show();
                    applyLeaveButton.setEnabled(true);
                });
    }

    private void saveLeaveData(String reason) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("leaveReason", reason);
        editor.putString("leaveStatus", "Pending");
        editor.putString("startDate", startDate);
        editor.putString("endDate", endDate);
        editor.apply();
    }

    private void loadLeaveData() {
        leaveReason.setText(sharedPreferences.getString("leaveReason", ""));
        startDate = sharedPreferences.getString("startDate", null);
        endDate = sharedPreferences.getString("endDate", null);
        leaveStatus.setText("Leave Status: " + sharedPreferences.getString("leaveStatus", "Not Applied"));

        if (startDate != null) {
            startDateButton.setText("Start Date: " + startDate);
        }
        if (endDate != null) {
            endDateButton.setText("End Date: " + endDate);
        }
    }
    private void fetchCurrentLeaveStatus() {
        db.collection("leave-requests")
                .whereEqualTo("employeeEmail", employeeEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = querySnapshots.getDocuments();

                        // Sort manually by startDate descending
                        documents.sort((doc1, doc2) -> {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Date d1 = sdf.parse(doc1.getString("startDate"));
                                Date d2 = sdf.parse(doc2.getString("startDate"));
                                return d2.compareTo(d1); // descending
                            } catch (Exception e) {
                                return 0;
                            }
                        });

                        DocumentSnapshot latestDoc = documents.get(0);
                        String status = latestDoc.getString("status");
                        updateLeaveStatusUI(status);
                    } else {
                        updateLeaveStatusUI("no request");
                    }
                })
                .addOnFailureListener(e -> {
                    updateLeaveStatusUI("error");
                    Log.e("LeaveStatus", "Failed to fetch leave status", e);
                });
    }



    private void updateLeaveStatusUI(String status) {
        String displayText;
        int colorRes;

        switch (status.toLowerCase()) {
            case "approved":
                displayText = "APPROVED";
                colorRes = R.color.success_color;
                break;
            case "rejected":
                displayText = "REJECTED";
                colorRes = R.color.error_color;
                break;
            case "pending":
                displayText = "PENDING";
                colorRes = R.color.warning_color;
                break;
            case "no request":
                displayText = "No current leave request";
                colorRes = R.color.text_secondary2;
                break;
            case "error":
                displayText = "Error fetching status";
                colorRes = R.color.error_color;
                break;
            default:
                displayText = "Unknown status";
                colorRes = R.color.text_secondary2;
                break;
        }

        leaveStatus.setText(displayText);
        leaveStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    private void loadLeaveHistory() {
        db.collection("leave-requests")
                .whereEqualTo("employeeEmail", employeeEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    leaveList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        LeaveHistory leave = doc.toObject(LeaveHistory.class);
                        if (leave != null) leaveList.add(leave);
                    }

                    // Sort by startDate descending
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    leaveList.sort((l1, l2) -> {
                        try {
                            Date d1 = sdf.parse(l1.getStartDate());
                            Date d2 = sdf.parse(l2.getStartDate());
                            return d2.compareTo(d1);
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    // Initially load top 3
                    updateVisibleList(3);
                })
                .addOnFailureListener(e -> {
                    Log.e("LeaveHistory", "Firestore error: ", e);
                    Toast.makeText(getContext(), "Error loading leave history", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateVisibleList(int count) {
        visibleLeaveList.clear();
        int limit = Math.min(count, leaveList.size());
        visibleLeaveList.addAll(leaveList.subList(0, limit));
        adapter.notifyDataSetChanged();
    }




}

