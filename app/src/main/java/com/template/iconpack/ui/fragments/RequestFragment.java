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
    private View bottomBar;
    private View btnSelectAll;
    private TextView selectedCountText;
    private RequestAppAdapter adapter;
    private List<AppInfo> allApps;
    private String currentFilter = "all";

    private TextView pillAll, pillThemed, pillUnthemed, pillSelected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        Context ctx = getContext();
        if (ctx == null) return view;

        // Stats
        bottomBar = view.findViewById(R.id.request_bottom_bar);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        selectedCountText = view.findViewById(R.id.selected_count_text);

        // Filter pills
        pillAll = view.findViewById(R.id.filter_all);
        pillThemed = view.findViewById(R.id.filter_themed);
        pillUnthemed = view.findViewById(R.id.filter_unthemed);
        pillSelected = view.findViewById(R.id.filter_selected);

        pillAll.setOnClickListener(v -> applyFilter("all"));
        pillThemed.setOnClickListener(v -> applyFilter("themed"));
        pillUnthemed.setOnClickListener(v -> applyFilter("unthemed"));
        pillSelected.setOnClickListener(v -> applyFilter("selected"));

        // List
        requestList = view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(ctx));

        allApps = AppScanner.scanInstalledApps(ctx);
        int total = allApps.size(), themed = 0;
        for (AppInfo a : allApps) if (a.isThemed) themed++;

        // Stats
        ((TextView) view.findViewById(R.id.stat_total)).setText(String.valueOf(total));
        ((TextView) view.findViewById(R.id.stat_themed)).setText(String.valueOf(themed));
        ((TextView) view.findViewById(R.id.stat_unthemed)).setText(String.valueOf(total - themed));

        adapter = new RequestAppAdapter(allApps);
        adapter.setSelectionListener(count -> updateBottomBar());
        requestList.setAdapter(adapter);

        // Buttons
        btnSelectAll.setOnClickListener(v -> adapter.selectAllUnthemed());
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportList());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareList());

        updateBottomBar();
        updatePills("all");
        return view;
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        adapter.setFilter(filter);
        updatePills(filter);
        updateBottomBar();
    }

    private void updatePills(String filter) {
        pillAll.setBackgroundResource(filter.equals("all") ? R.drawable.glass_chip_selected
                : R.drawable.glass_chip_unselected);
        pillAll.setTextColor(filter.equals("all") ? 0xFFFFFFFF : 0xFF4A4A6A);

        pillThemed.setBackgroundResource(filter.equals("themed") ? R.drawable.glass_chip_selected
                : R.drawable.glass_chip_unselected);
        pillThemed.setTextColor(filter.equals("themed") ? 0xFFFFFFFF : 0xFF4A4A6A);

        pillUnthemed.setBackgroundResource(filter.equals("unthemed") ? R.drawable.glass_chip_selected
                : R.drawable.glass_chip_unselected);
        pillUnthemed.setTextColor(filter.equals("unthemed") ? 0xFFFFFFFF : 0xFF4A4A6A);

        pillSelected.setBackgroundResource(filter.equals("selected") ? R.drawable.glass_chip_selected
                : R.drawable.glass_chip_unselected);
        pillSelected.setTextColor(filter.equals("selected") ? 0xFFFFFFFF : 0xFF4A4A6A);
    }

    private void updateBottomBar() {
        boolean show = currentFilter.equals("unthemed") || currentFilter.equals("selected");
        bottomBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            int count = adapter.getSelectedCount();
            selectedCountText.setText("Selected: " + count);
            btnSelectAll.setVisibility(currentFilter.equals("unthemed") ? View.VISIBLE : View.GONE);
        }
    }

    private void exportList() {
        List<AppInfo> selected = adapter.getSelectedApps();
        if (selected.isEmpty()) {
            Toast.makeText(getContext(), "No apps selected", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (AppInfo a : selected) {
            sb.append(a.appName).append(" | ").append(a.packageName).append("\n");
        }
        ClipboardManager cm = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("icon_request", sb.toString()));
        Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void shareList() {
        List<AppInfo> selected = adapter.getSelectedApps();
        if (selected.isEmpty()) {
            Toast.makeText(getContext(), "No apps selected", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (AppInfo a : selected) {
            sb.append(a.appName).append(" | ").append(a.packageName)
                    .append(" | ").append(a.activityName).append("\n");
        }
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_request_subject));
        i.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(i, getString(R.string.request_share)));
    }
}
