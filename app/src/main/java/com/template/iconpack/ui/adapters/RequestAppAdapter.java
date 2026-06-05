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

public class RequestAppAdapter extends RecyclerView.Adapter<RequestAppAdapter.AppHolder> {

    private final List<AppInfo> allApps;
    private final List<AppInfo> filteredApps;
    private String currentFilter = "all";
    private boolean showCheckboxes = false;

    private SelectionListener selectionListener;

    public interface SelectionListener { void onSelectionChanged(int selectedCount); }

    public RequestAppAdapter(List<AppInfo> apps) {
        this.allApps = apps;
        this.filteredApps = new ArrayList<>(apps);
    }

    public void setSelectionListener(SelectionListener l) { this.selectionListener = l; }

    public void setFilter(String filter) {
        currentFilter = filter;
        applyFilter();
    }

    public void selectAllUnthemed() {
        for (AppInfo a : filteredApps) {
            if (!a.isThemed) a.isSelected = true;
        }
        notifyDataSetChanged();
        fireSelectionChanged();
    }

    private void fireSelectionChanged() {
        if (selectionListener != null) selectionListener.onSelectionChanged(getSelectedCount());
    }

    public int getSelectedCount() {
        int c = 0;
        for (AppInfo a : filteredApps) if (a.isSelected) c++;
        return c;
    }

    public boolean isShowCheckboxes() { return showCheckboxes; }

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
        }
        showCheckboxes = currentFilter.equals("unthemed") || currentFilter.equals("selected");
        if (!showCheckboxes) for (AppInfo a : allApps) a.isSelected = false;
        notifyDataSetChanged();
        fireSelectionChanged();
    }

    public List<AppInfo> getSelectedApps() {
        List<AppInfo> sel = new ArrayList<>();
        for (AppInfo a : filteredApps) if (a.isSelected) sel.add(a);
        return sel;
    }

    @Override public int getItemCount() { return filteredApps.size(); }

    @NonNull @Override
    public AppHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppHolder h, int pos) {
        AppInfo app = filteredApps.get(pos);
        h.name.setText(app.appName);
        h.pkg.setText(app.packageName);
        h.checkbox.setVisibility(showCheckboxes && !app.isThemed ? View.VISIBLE : View.GONE);
        h.checkbox.setChecked(app.isSelected);

        if (app.isThemed) {
            h.status.setText("已适配");
            h.status.setBackgroundColor(0xFFE8F5E9);
            h.status.setTextColor(0xFF388E3C);
        } else {
            h.status.setText("未适配");
            h.status.setBackgroundColor(0xFFFFEBEE);
            h.status.setTextColor(0xFFE53935);
        }

        // Row click toggles selection
        View.OnClickListener toggle = v -> {
            if (!showCheckboxes || app.isThemed) return;
            app.isSelected = !app.isSelected;
            h.checkbox.setChecked(app.isSelected);
            fireSelectionChanged();
        };
        h.itemView.setOnClickListener(toggle);
        // Checkbox click also syncs
        h.checkbox.setOnClickListener(v -> {
            app.isSelected = h.checkbox.isChecked();
            fireSelectionChanged();
        });
    }

    static class AppHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, pkg, status;
        CheckBox checkbox;
        AppHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.request_icon);
            name = v.findViewById(R.id.request_app_name);
            pkg = v.findViewById(R.id.request_package_name);
            status = v.findViewById(R.id.request_status);
            checkbox = v.findViewById(R.id.request_checkbox);
        }
    }
}
