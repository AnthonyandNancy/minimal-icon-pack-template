package com.template.iconpack.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.ui.adapters.RequestAppAdapter;
import com.template.iconpack.utils.AppScanner;

import java.util.List;

public class RequestFragment extends Fragment {

    private RecyclerView requestList;
    private TextView statTotal, statThemed, statUnthemed;
    private ProgressBar progressBar;
    private View bottomBar;
    private TextView selectedCountView;
    private RequestAppAdapter adapter;
    private List<AppInfo> allApps;

    private TextView filterAll, filterThemed, filterUnthemed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        statTotal = view.findViewById(R.id.stat_total);
        statThemed = view.findViewById(R.id.stat_themed);
        statUnthemed = view.findViewById(R.id.stat_unthemed);
        progressBar = view.findViewById(R.id.request_progress);
        bottomBar = view.findViewById(R.id.request_bottom_bar);
        selectedCountView = view.findViewById(R.id.request_selected_count);

        requestList = view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Filter chips (now TextViews, not MaterialButtons)
        filterAll = view.findViewById(R.id.filter_all);
        filterThemed = view.findViewById(R.id.filter_themed);
        filterUnthemed = view.findViewById(R.id.filter_unthemed);

        if (getContext() != null) {
            allApps = AppScanner.scanInstalledApps(getContext());
            updateStats();
            adapter = new RequestAppAdapter(allApps);
            requestList.setAdapter(adapter);
        }

        // Filter listeners
        filterAll.setOnClickListener(v -> {
            if (adapter != null) { adapter.setFilter("all"); adapter.setShowCheckboxes(false); }
            updateFilterUI("all");
            bottomBar.setVisibility(View.GONE);
        });
        filterThemed.setOnClickListener(v -> {
            if (adapter != null) { adapter.setFilter("themed"); adapter.setShowCheckboxes(false); }
            updateFilterUI("themed");
            bottomBar.setVisibility(View.GONE);
        });
        filterUnthemed.setOnClickListener(v -> {
            if (adapter != null) { adapter.setFilter("unthemed"); adapter.setShowCheckboxes(true); }
            updateFilterUI("unthemed");
            updateBottomBar();
        });

        // Export & share
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportList());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareList());

        // Update bottom bar on selection change via delayed check
        requestList.postDelayed(() -> updateBottomBar(), 500);

        return view;
    }

    private void updateStats() {
        int total = allApps.size();
        int themed = 0;
        for (AppInfo app : allApps) {
            if (app.isThemed) themed++;
        }
        int unthemed = total - themed;

        statTotal.setText(String.valueOf(total));
        statThemed.setText(String.valueOf(themed));
        statUnthemed.setText(String.valueOf(unthemed));

        if (total > 0) {
            progressBar.setMax(total);
            progressBar.setProgress(themed);
        }
    }

    private void updateFilterUI(String active) {
        int selColor = getResources().getColor(R.color.text_on_primary);
        int unselColor = getResources().getColor(R.color.text_secondary);
        int selBg = R.drawable.glass_chip_selected;
        int unselBg = R.drawable.glass_chip_unselected;

        filterAll.setTextColor(active.equals("all") ? selColor : unselColor);
        filterAll.setBackgroundResource(active.equals("all") ? selBg : unselBg);

        filterThemed.setTextColor(active.equals("themed") ? selColor : unselColor);
        filterThemed.setBackgroundResource(active.equals("themed") ? selBg : unselBg);

        filterUnthemed.setTextColor(active.equals("unthemed") ? selColor : unselColor);
        filterUnthemed.setBackgroundResource(active.equals("unthemed") ? selBg : unselBg);
    }

    private void updateBottomBar() {
        if (adapter == null) return;
        List<AppInfo> selected = adapter.getSelectedApps();
        if (selected.isEmpty()) {
            bottomBar.setVisibility(View.GONE);
        } else {
            bottomBar.setVisibility(View.VISIBLE);
            selectedCountView.setText("已选 " + selected.size() + " 项");
        }
    }

    private void exportList() {
        if (adapter == null) return;
        List<AppInfo> selected = adapter.getSelectedApps();
        if (selected.isEmpty()) {
            for (AppInfo app : allApps) { if (!app.isThemed) selected.add(app); }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("图标包适配申请列表\n==================\n\n");
        for (AppInfo app : selected) {
            sb.append("应用名: ").append(app.appName).append("\n");
            sb.append("包名: ").append(app.packageName).append("\n");
            sb.append("Component: ").append(app.getComponentInfo()).append("\n---\n");
        }

        if (getContext() != null) {
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("request_list", sb.toString()));
            Toast.makeText(getContext(), "申请列表已复制到剪贴板", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareList() {
        if (adapter == null) return;
        List<AppInfo> selected = adapter.getSelectedApps();
        if (selected.isEmpty()) {
            for (AppInfo app : allApps) { if (!app.isThemed) selected.add(app); }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("图标包适配申请列表\n\n");
        for (AppInfo app : selected) {
            sb.append(app.appName).append(" | ").append(app.getComponentInfo()).append("\n");
        }

        if (getContext() != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_request_subject));
            intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            startActivity(Intent.createChooser(intent, "分享申请列表"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestList.postDelayed(() -> updateBottomBar(), 300);
    }
}
