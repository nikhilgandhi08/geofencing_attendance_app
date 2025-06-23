package com.example.app006.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.typeTextView.setText(report.getType());
        holder.titleTextView.setText(report.getTitle());
        holder.dateTextView.setText(report.getDate());
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView typeTextView, titleTextView, dateTextView;

        public ReportViewHolder(View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.reportType);
            titleTextView = itemView.findViewById(R.id.reportTitle);
            dateTextView = itemView.findViewById(R.id.reportDate);
        }
    }
}
