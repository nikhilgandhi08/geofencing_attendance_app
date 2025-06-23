package com.example.app006.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.AdminEmp;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {
    private final List<AdminEmp> employeeList;

    public EmployeeAdapter(List<AdminEmp> employeeList) {
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_empmgmt_recycler_view, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        holder.bind(employeeList.get(position));
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmployeeEmail;

        EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeEmail = itemView.findViewById(R.id.tv_emp_email);
        }

        void bind(AdminEmp employee) {
            tvEmployeeEmail.setText(employee.getEmpEmail());
        }
    }
}
