package com.example.app006.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.LeaveHistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LeaveHistoryAdapter extends RecyclerView.Adapter<LeaveHistoryAdapter.LeaveViewHolder> {

    private List<LeaveHistory> leaveList;

    public LeaveHistoryAdapter(List<LeaveHistory> leaveList) {
        this.leaveList = leaveList;
    }

    public void updateData(List<LeaveHistory> newList) {
        this.leaveList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leave_history_item, parent, false);
        return new LeaveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveViewHolder holder, int position) {
        LeaveHistory leave = leaveList.get(position);
        holder.tvReason.setText(leave.getReason());
        holder.tvDateRange.setText(leave.getStartDate() + " - " + leave.getEndDate());

        int days = calculateDaysBetween(leave.getStartDate(), leave.getEndDate());
        holder.tvDuration.setText(days + " days");

        // Set status color/icon
        switch (leave.getStatus().toLowerCase()) {
            case "approved":
                holder.tvStatus.setText("APPROVED");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_color2));
                holder.statusIcon.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.success_color_light));
                break;
            case "pending":
                holder.tvStatus.setText("PENDING");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning_color));
                holder.statusIcon.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.warning_color_light));
                break;
            case "rejected":
                holder.tvStatus.setText("REJECTED");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_color3));
                holder.statusIcon.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.accent_light));
                break;
        }
    }

    private int calculateDaysBetween(String start, String end) {
        if (start == null || end == null) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            if (startDate == null || endDate == null) return 0;

            long diff = endDate.getTime() - startDate.getTime();
            return (int) (TimeUnit.MILLISECONDS.toDays(diff) + 1); // Include both start and end days
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    static class LeaveViewHolder extends RecyclerView.ViewHolder {
        TextView tvReason, tvDateRange, tvDuration, tvStatus;
        LinearLayout statusIcon;

        LeaveViewHolder(View itemView) {
            super(itemView);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvDateRange = itemView.findViewById(R.id.tv_date_range);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvStatus = itemView.findViewById(R.id.tv_status);
            statusIcon = itemView.findViewById(R.id.status_icon_container);
        }
    }
}


