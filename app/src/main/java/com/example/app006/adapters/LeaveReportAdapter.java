package com.example.app006.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.LeaveRequestNew;

import java.util.List;

public class LeaveReportAdapter extends RecyclerView.Adapter<LeaveReportAdapter.ViewHolder> {

    private List<LeaveRequestNew> leaveList;
    private Context context;

    public LeaveReportAdapter(List<LeaveRequestNew> leaveList, Context context) {
        this.leaveList = leaveList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leave_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaveRequestNew leave = leaveList.get(position);
        holder.tvEmployeeName.setText(leave.getEmployeeName());
        holder.tvLeaveReason.setText("Reason: " + leave.getReason());
        holder.tvLeaveDates.setText("From: " + leave.getStartDate() + "  To: " + leave.getEndDate());
        holder.tvLeaveStatus.setText("Status: " + leave.getStatus());
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvLeaveReason, tvLeaveDates, tvLeaveStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvLeaveReason = itemView.findViewById(R.id.tvLeaveReason);
            tvLeaveDates = itemView.findViewById(R.id.tvLeaveDates);
            tvLeaveStatus = itemView.findViewById(R.id.tvLeaveStatus);
        }
    }


}
