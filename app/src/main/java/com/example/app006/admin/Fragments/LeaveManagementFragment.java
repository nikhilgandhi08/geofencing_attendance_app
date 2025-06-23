package com.example.app006.admin.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.adapters.LeaveRequestAdapter;
import com.example.app006.models.LeaveRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaveManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private LeaveRequestAdapter adapter;
    private List<LeaveRequest> leaveRequestList;
    private FirebaseFirestore firestore;

    public LeaveManagementFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_leave, container, false);

        recyclerView = view.findViewById(R.id.rv_leave_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();
        leaveRequestList = new ArrayList<>();
        adapter = new LeaveRequestAdapter(getContext(), leaveRequestList, new LeaveRequestAdapter.OnLeaveRequestActionListener() {
            @Override
            public void onApproveClick(LeaveRequest leaveRequest, int position) {
                updateLeaveStatus(leaveRequest, "Approved", position);
            }

            @Override
            public void onRejectClick(LeaveRequest leaveRequest, int position) {
                updateLeaveStatus(leaveRequest, "Rejected", position);
            }
        });

        recyclerView.setAdapter(adapter);

        fetchPendingRequests(); // Fetch leave requests from Firebase

        return view;
    }

    private void fetchPendingRequests() {
        CollectionReference leaveRequestsRef = firestore.collection("leave-requests");

        leaveRequestsRef
                .whereEqualTo("status", "Pending") // Ensure Firestore has 'status' field
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("FirestoreError", "Error fetching data: ", error);
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value == null || value.isEmpty()) {
                            Toast.makeText(getContext(), "No pending leave requests found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        leaveRequestList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            LeaveRequest leaveRequest = document.toObject(LeaveRequest.class);
                            if (leaveRequest != null) {
                                leaveRequest.setRequestId(document.getId()); // Store document ID for updates
                                leaveRequestList.add(leaveRequest);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void updateLeaveStatus(LeaveRequest leaveRequest, String newStatus, int position) {
        String docId = leaveRequest.getRequestId();

        firestore.collection("leave-requests").document(docId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Leave " + newStatus, Toast.LENGTH_SHORT).show();
                    leaveRequestList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to update status", e);
                    Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }
}
