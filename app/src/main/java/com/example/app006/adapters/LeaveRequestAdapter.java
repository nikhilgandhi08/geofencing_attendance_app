package com.example.app006.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.LeaveRequest;

import java.util.List;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder> {

    private Context context;
    private List<LeaveRequest> leaveRequestList;
    private OnLeaveRequestActionListener actionListener;

    // Interface for handling button clicks
    public interface OnLeaveRequestActionListener {
        void onApproveClick(LeaveRequest leaveRequest, int position);
        void onRejectClick(LeaveRequest leaveRequest, int position);
    }

    public LeaveRequestAdapter(Context context, List<LeaveRequest> leaveRequestList, OnLeaveRequestActionListener actionListener) {
        this.context = context;
        this.leaveRequestList = leaveRequestList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leave_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaveRequest leaveRequest = leaveRequestList.get(position);

        holder.tvEmployeeEmail.setText(leaveRequest.getEmployeeEmail());
        holder.tvReason.setText("Reason: " + leaveRequest.getReason());
        holder.tvDateRange.setText(leaveRequest.getStartDate() + " - " + leaveRequest.getEndDate());

        // Approve Button Click
        holder.btnApprove.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onApproveClick(leaveRequest, position);
            }
        });

        // Reject Button Click
        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRejectClick(leaveRequest, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return leaveRequestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeEmail, tvReason, tvDateRange;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeEmail = itemView.findViewById(R.id.tv_employee_email);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvDateRange = itemView.findViewById(R.id.tv_date_range);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
