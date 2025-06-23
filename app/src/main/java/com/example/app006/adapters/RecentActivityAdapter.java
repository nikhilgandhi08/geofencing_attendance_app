package com.example.app006.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.ActivityItem;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private final List<ActivityItem> activityList;

    public RecentActivityAdapter(List<ActivityItem> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityItem item = activityList.get(position);

        // Set Activity Title
        String title = item.getType().equalsIgnoreCase("login") ?
                "Employee Checked In" : "Employee Checked Out";
        holder.tvActivityTitle.setText(title);

        // Set Activity Description
        String description = item.getEmail() + " " + item.getType() + " at " + item.getTime();
        holder.tvActivityDescription.setText(description);

        // Set Email
        holder.tvEmployeeEmail.setText(item.getEmail());

        // Status - let's assume all successful for simplicity
        holder.tvActivityStatus.setText("SUCCESS");
        holder.tvActivityStatus.setVisibility(View.VISIBLE);

        // Timestamp (you can format as needed)
        holder.tvActivityTimestamp.setText(item.getDate());

        // Location Info - show only if insideGeofence true
        if (item.isInsideGeofence()) {
            holder.llLocationInfo.setVisibility(View.VISIBLE);
            holder.tvGeofenceStatus.setText("Inside");
            holder.tvGeofenceStatus.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(R.color.success_color));
        } else {
            holder.llLocationInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivActivityIcon;
        TextView tvActivityTitle, tvActivityDescription, tvEmployeeEmail,
                tvActivityStatus, tvActivityTimestamp, tvGeofenceStatus;
        LinearLayout llLocationInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActivityIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvActivityTitle = itemView.findViewById(R.id.tv_activity_title);
            tvActivityDescription = itemView.findViewById(R.id.tv_activity_description);
            tvEmployeeEmail = itemView.findViewById(R.id.tv_employee_email);
            tvActivityStatus = itemView.findViewById(R.id.tv_activity_status);
            tvActivityTimestamp = itemView.findViewById(R.id.tv_activity_timestamp);
            llLocationInfo = itemView.findViewById(R.id.ll_location_info);
            tvGeofenceStatus = itemView.findViewById(R.id.tv_geofence_status);
        }
    }
}

