package com.template.iconpack.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private RequestAppAdapter adapter;
    private List<AppInfo> allApps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        statTotal = view.findViewById(R.id.stat_total);
        statThemed = view.findViewById(R.id.stat_themed);
        statUnthemed = view.findViewById(R.id.stat_unthemed);

        requestList = view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load apps
        if (getContext() != null) {
            allApps = AppScanner.scanInstalledApps(getContext());
            updateStats();
            adapter = new RequestAppAdapter(allApps);
            requestList.setAdapter(adapter);
        }

        // Filter buttons
        view.findViewById(R.id.filter_all).setOnClickListener(v -> {
            if (adapter != null) adapter.setFilter("all");
            updateFilterButtons(view, "all");
        });
        view.findViewById(R.id.filter_themed).setOnClickListener(v -> {
            if (adapter != null) adapter.setFilter("themed");
            updateFilterButtons(view, "themed");
        });
        view.findViewById(R.id.filter_unthemed).setOnClickListener(v -> {
            if (adapter != null) {
                adapter.setFilter("unthemed");
                adapter.setShowCheckboxes(true);
            }
            updateFilterButtons(view, "unthemed");
        });

        // Export
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportList());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareList());

        return view;
    }

    private void updateStats() {
        int total = allApps.size();
        int themed = 0;
        for (AppInfo app : allApps) {
            if (app.isThemed) themed++;
        }
        int unthemed = total - themed;

        statTotal.setText("总计: " + total);
        statThemed.setText("已适配: " + themed);
        statUnthemed.setText("未适配: " + unthemed);
    }

    private void updateFilterButtons(View view, String active) {
        view.findViewById(R.id.filter_all).setSelected(active.equals("all"));
        view.findViewById(R.id.filter_themed).setSelected(active.equals("themed"));
        view.findViewById(R.id.filter_unthemed).setSelected(active.equals("unthemed"));

        if (!active.equals("unthemed") && adapter != null) {
            adapter.setShowCheckboxes(false);
        }
    }

    private void exportList() {
        if (adapter == null) return;
        List<AppInfo> selected = adapter.getSelectedApps();
        if (selected.isEmpty()) {
            // If nothing selected, export all unthemed
            for (AppInfo app : allApps) {
                if (!app.isThemed) selected.add(app);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("图标包适配申请列表\n");
        sb.append("==================\n\n");
        for (AppInfo app : selected) {
            sb.append("应用名: ").append(app.appName).append("\n");
            sb.append("包名: ").append(app.packageName).append("\n");
            sb.append("Component: ").append(app.getComponentInfo()).append("\n");
            sb.append("---\n");
        }

        if (getContext() != null) {
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("request_list", sb.toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "申请列表已复制到剪贴板", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareList() {
        if (adapter == null) return;
        List<AppInfo> selected = adapter.getSelectedApps();
        if (selected.isEmpty()) {
            for (AppInfo app : allApps) {
                if (!app.isThemed) selected.add(app);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("图标包适配申请列表\n\n");
        for (AppInfo app : selected) {
            sb.append(app.appName).append(" | ")
              .append(app.getComponentInfo()).append("\n");
        }

        if (getContext() != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_request_subject));
            intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            startActivity(Intent.createChooser(intent, "分享申请列表"));
        }
    }
}
