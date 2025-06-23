package com.example.app006.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.Payroll;

import java.util.List;

public class PayrollAdapter extends RecyclerView.Adapter<PayrollAdapter.PayrollViewHolder> {

    private final List<Payroll> payrollList;

    public PayrollAdapter(List<Payroll> payrollList) {
        this.payrollList = payrollList;
    }

    @NonNull
    @Override
    public PayrollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payroll, parent, false);
        return new PayrollViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PayrollViewHolder holder, int position) {
        Payroll payroll = payrollList.get(position);

        holder.textMonthYear.setText(payroll.getMonthYear());
        holder.textBasicSalary.setText("₹ " + String.format("%.2f", payroll.getBasicSalary()));
        holder.textAllowance.setText("₹ " + String.format("%.2f", payroll.getAllowance()));
        holder.textDeduction.setText("₹ " + String.format("%.2f", payroll.getDeduction()));
        holder.textBonus.setText("₹ " + String.format("%.2f", payroll.getBonus()));
        holder.textDaysPresent.setText(" " + String.valueOf(payroll.getDaysPresent()));
        holder.textTotalWorkingDays.setText(" " + String.valueOf(payroll.getTotalWorkingDays()));
        holder.textNetSalary.setText("₹ " + String.format("%.2f", payroll.getNetSalary()));
    }

    @Override
    public int getItemCount() {
        return payrollList.size();
    }

    static class PayrollViewHolder extends RecyclerView.ViewHolder {

        TextView textMonthYear, textBasicSalary, textAllowance, textDeduction, textBonus;
        TextView textDaysPresent, textTotalWorkingDays, textNetSalary;

        public PayrollViewHolder(@NonNull View itemView) {
            super(itemView);

            textMonthYear = itemView.findViewById(R.id.text_monthYear);
            textBasicSalary = itemView.findViewById(R.id.text_basicSalary);
            textAllowance = itemView.findViewById(R.id.text_allowance);
            textDeduction = itemView.findViewById(R.id.text_deduction);
            textBonus = itemView.findViewById(R.id.text_bonus);
            textDaysPresent = itemView.findViewById(R.id.text_daysPresent);
            textTotalWorkingDays = itemView.findViewById(R.id.text_totalWorkingDays);
            textNetSalary = itemView.findViewById(R.id.text_netSalary);
        }
    }
}
