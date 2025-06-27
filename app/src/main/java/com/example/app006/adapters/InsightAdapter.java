package com.example.app006.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.models.Insight;

import java.util.List;

public class InsightAdapter extends RecyclerView.Adapter<InsightAdapter.InsightViewHolder> {

    private final List<Insight> insightList;

    public InsightAdapter(List<Insight> insightList) {
        this.insightList = insightList;
    }

    @NonNull
    @Override
    public InsightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.insight_item_layout, parent, false);
        return new InsightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InsightViewHolder holder, int position) {
        Insight insight = insightList.get(position);
        holder.tvMessage.setText(insight.getMessage());

        if (insight.getSubtext() != null && !insight.getSubtext().isEmpty()) {
            holder.tvSubtext.setText(insight.getSubtext());
            holder.tvSubtext.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubtext.setVisibility(View.GONE);
        }

        holder.ivIcon.setImageResource(insight.getIconResId());

        Drawable bg = holder.ivIcon.getBackground();
        if (bg != null) {
            bg.setTint(ContextCompat.getColor(holder.itemView.getContext(), insight.getBackgroundColorResId()));
        }
    }

    @Override
    public int getItemCount() {
        return insightList.size();
    }

    static class InsightViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvMessage, tvSubtext;

        InsightViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_insight_icon);
            tvMessage = itemView.findViewById(R.id.tv_insight_text);
            tvSubtext = itemView.findViewById(R.id.tv_insight_subtext);
        }
    }
}

