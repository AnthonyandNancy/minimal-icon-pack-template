package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class RequestAppAdapter extends RecyclerView.Adapter<RequestAppAdapter.ViewHolder> {

    private final List<AppInfo> allApps;
    private final List<AppInfo> filteredApps;
    private String currentFilter = "all"; // "all", "themed", "unthemed"
    private boolean showCheckboxes = false;

    public RequestAppAdapter(List<AppInfo> apps) {
        this.allApps = apps;
        this.filteredApps = new ArrayList<>(apps);
    }

    public void setFilter(String filter) {
        currentFilter = filter;
        applyFilter();
    }

    public void setShowCheckboxes(boolean show) {
        this.showCheckboxes = show;
        if (!show) {
            for (AppInfo app : allApps) app.isSelected = false;
        }
        notifyDataSetChanged();
    }

    public void notifyDataChanged() { notifyDataSetChanged(); }

    private void applyFilter() {
        filteredApps.clear();
        switch (currentFilter) {
            case "themed":
                for (AppInfo a : allApps) { if (a.isThemed) filteredApps.add(a); }
                break;
            case "unthemed":
                for (AppInfo a : allApps) { if (!a.isThemed) filteredApps.add(a); }
                break;
            case "selected":
                for (AppInfo a : allApps) { if (a.isSelected) filteredApps.add(a); }
                break;
            default:
                filteredApps.addAll(allApps);
                break;
        }
        notifyDataSetChanged();
    }

    public List<AppInfo> getSelectedApps() {
        List<AppInfo> selected = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (app.isSelected) selected.add(app);
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = filteredApps.get(position);
        holder.appName.setText(app.appName);
        holder.packageName.setText(app.packageName);

        // Status badge
        if (app.isThemed) {
            holder.status.setText("已适配");
            holder.status.setBackgroundResource(R.drawable.status_badge);
            holder.status.setVisibility(View.VISIBLE);
        } else {
            holder.status.setText("未适配");
            holder.status.setBackgroundResource(R.drawable.status_badge);
            holder.status.setVisibility(View.VISIBLE);
        }

        // Checkbox
        holder.checkbox.setVisibility(showCheckboxes ? View.VISIBLE : View.GONE);
        holder.checkbox.setChecked(app.isSelected);
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.isSelected = isChecked;
        });

        // App icon
        try {
            holder.icon.setImageDrawable(
                    holder.itemView.getContext().getPackageManager()
                            .getApplicationIcon(app.packageName));
        } catch (Exception e) {
            holder.icon.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        holder.itemView.setOnClickListener(v -> {
            if (showCheckboxes) {
                app.isSelected = !app.isSelected;
                holder.checkbox.setChecked(app.isSelected);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredApps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        ImageView icon;
        TextView appName;
        TextView packageName;
        TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.request_checkbox);
            icon = itemView.findViewById(R.id.request_icon);
            appName = itemView.findViewById(R.id.request_app_name);
            packageName = itemView.findViewById(R.id.request_package_name);
            status = itemView.findViewById(R.id.request_status);
        }
    }
}
