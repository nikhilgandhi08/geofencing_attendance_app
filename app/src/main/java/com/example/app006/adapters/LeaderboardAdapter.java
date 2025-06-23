package com.example.app006.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.LeaderboardEmployee;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardEmployee> employeeList;
    private Context context;

    public LeaderboardAdapter(List<LeaderboardEmployee> employeeList, Context context) {
        this.employeeList = employeeList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_reports_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEmployee employee = employeeList.get(position);

        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvEmployeeName.setText(employee.getName());
        holder.tvEmployeeEmail.setText(employee.getEmail());
        holder.tvAttendanceCount.setText(String.valueOf(employee.getLoginCount()));

        // Show trophy for top 3
        holder.ivTrophy.setVisibility(position < 3 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvEmployeeName, tvEmployeeEmail, tvAttendanceCount;
        ImageView ivTrophy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeEmail = itemView.findViewById(R.id.tvEmployeeEmail);
            tvAttendanceCount = itemView.findViewById(R.id.tvAttendanceCount);
            ivTrophy = itemView.findViewById(R.id.ivTrophy);
        }
    }
}
