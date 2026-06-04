package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class RequestAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_STATS  = 0;
    private static final int TYPE_FILTER = 1;
    private static final int TYPE_APP    = 2;

    private final List<AppInfo> allApps;
    private final List<AppInfo> filteredApps;
    private String currentFilter = "all";
    private boolean showCheckboxes = false;

    private int totalApps, themedCount, unthemedCount;
    private FilterClickListener filterListener;
    private AppClickListener appClickListener;

    public interface FilterClickListener { void onFilterClicked(String filter); }
    public interface AppClickListener { void onAppClicked(AppInfo app, int position); }

    public RequestAppAdapter(List<AppInfo> apps) {
        this.allApps = apps;
        this.filteredApps = new ArrayList<>(apps);
    }

    public void setStats(int total, int themed, int unthemed) {
        this.totalApps = total;
        this.themedCount = themed;
        this.unthemedCount = unthemed;
    }

    public void setFilterListener(FilterClickListener l) { this.filterListener = l; }
    public void setAppClickListener(AppClickListener l) { this.appClickListener = l; }

    public void setFilter(String filter) {
        currentFilter = filter;
        applyFilter();
    }

    public void setShowCheckboxes(boolean show) {
        this.showCheckboxes = show;
        if (!show) for (AppInfo a : allApps) a.isSelected = false;
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
            default:
                filteredApps.addAll(allApps);
        }
        notifyDataSetChanged();
    }

    public List<AppInfo> getSelectedApps() {
        List<AppInfo> sel = new ArrayList<>();
        for (AppInfo a : filteredApps) if (a.isSelected) sel.add(a);
        return sel;
    }

    @Override public int getItemViewType(int position) {
        if (position == 0) return TYPE_STATS;
        if (position == 1) return TYPE_FILTER;
        return TYPE_APP;
    }

    @Override public int getItemCount() {
        return 2 + filteredApps.size(); // stats + filter + apps
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_STATS) {
            return new StatsHolder(inf.inflate(R.layout.item_request_stats, parent, false));
        }
        if (viewType == TYPE_FILTER) {
            return new FilterHolder(inf.inflate(R.layout.item_request_filters, parent, false));
        }
        return new AppHolder(inf.inflate(R.layout.item_app_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (holder instanceof StatsHolder) {
            StatsHolder h = (StatsHolder) holder;
            h.total.setText(String.valueOf(totalApps));
            h.themed.setText(String.valueOf(themedCount));
            h.unthemed.setText(String.valueOf(unthemedCount));
            if (totalApps > 0) {
                h.progress.setMax(totalApps);
                h.progress.setProgress(themedCount);
            }
        } else if (holder instanceof FilterHolder) {
            FilterHolder h = (FilterHolder) holder;
            h.filterAll.setOnClickListener(v -> clickFilter("all"));
            h.filterThemed.setOnClickListener(v -> clickFilter("themed"));
            h.filterUnthemed.setOnClickListener(v -> clickFilter("unthemed"));
        } else if (holder instanceof AppHolder) {
            AppInfo app = filteredApps.get(pos - 2);
            AppHolder h = (AppHolder) holder;
            h.name.setText(app.appName);
            h.pkg.setText(app.packageName);
            h.checkbox.setVisibility(showCheckboxes && !app.isThemed ? View.VISIBLE : View.GONE);
            h.checkbox.setChecked(app.isSelected);

            if (app.isThemed) {
                h.status.setText("已适配");
                h.status.setBackgroundResource(R.drawable.glass_badge_green);
                h.status.setTextColor(0xFFFFFFFF);
            } else {
                h.status.setText("未适配");
                h.status.setBackgroundResource(R.drawable.glass_badge_blue);
                h.status.setTextColor(0xFFFFFFFF);
            }

            h.itemView.setOnClickListener(v -> {
                if (showCheckboxes && !app.isThemed) {
                    app.isSelected = !app.isSelected;
                    h.checkbox.setChecked(app.isSelected);
                    if (appClickListener != null) appClickListener.onAppClicked(app, pos);
                    notifyItemChanged(pos);
                }
            });
        }
    }

    private void clickFilter(String filter) {
        setFilter(filter);
        setShowCheckboxes(filter.equals("unthemed"));
        if (filterListener != null) filterListener.onFilterClicked(filter);
    }

    // ── ViewHolders ─────────────────────────────────────

    static class StatsHolder extends RecyclerView.ViewHolder {
        TextView total, themed, unthemed;
        ProgressBar progress;
        StatsHolder(View v) {
            super(v);
            total = v.findViewById(R.id.stat_total);
            themed = v.findViewById(R.id.stat_themed);
            unthemed = v.findViewById(R.id.stat_unthemed);
            progress = v.findViewById(R.id.stats_progress);
        }
    }

    static class FilterHolder extends RecyclerView.ViewHolder {
        TextView filterAll, filterThemed, filterUnthemed;
        FilterHolder(View v) {
            super(v);
            filterAll = v.findViewById(R.id.filter_all);
            filterThemed = v.findViewById(R.id.filter_themed);
            filterUnthemed = v.findViewById(R.id.filter_unthemed);
        }
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
