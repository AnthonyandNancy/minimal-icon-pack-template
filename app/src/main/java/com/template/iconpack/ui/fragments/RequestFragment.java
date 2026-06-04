package com.template.iconpack.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.MainActivity;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.ui.LiquidGlassDrawable;
import com.template.iconpack.ui.adapters.RequestAppAdapter;
import com.template.iconpack.utils.AppScanner;

import java.util.ArrayList;
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
    private TextView filterSelected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        Context ctx = getContext();
        if (ctx == null) return view;

        float density = ctx.getResources().getDisplayMetrics().density;

        statTotal   = view.findViewById(R.id.stat_total);
        statThemed  = view.findViewById(R.id.stat_themed);
        statUnthemed = view.findViewById(R.id.stat_unthemed);
        progressBar = view.findViewById(R.id.request_progress);
        bottomBar   = view.findViewById(R.id.request_bottom_bar);
        selectedCountView = view.findViewById(R.id.request_selected_count);

        bottomBar.setBackground(LiquidGlassDrawable.floatingBar(density));

        requestList = view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(ctx));

        filterAll      = view.findViewById(R.id.filter_all);
        filterThemed   = view.findViewById(R.id.filter_themed);
        filterUnthemed = view.findViewById(R.id.filter_unthemed);
        filterSelected = (TextView) view.findViewById(R.id.filter_selected);

        // Back button
        ImageButton btnBack = view.findViewById(R.id.btn_back_req);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Status spacer
        View spacer = view.findViewById(R.id.status_spacer_req);
        if (spacer != null && getActivity() instanceof MainActivity) {
            MainActivity ma = (MainActivity) getActivity();
            spacer.getLayoutParams().height = ma.getStatusBarHeight() + ma.dp(8);
            spacer.requestLayout();
        }

        allApps = AppScanner.scanInstalledApps(ctx);
        updateStats();
        adapter = new RequestAppAdapter(allApps);
        requestList.setAdapter(adapter);

        // Filters — bottom bar always visible
        filterAll.setOnClickListener(v -> {
            adapter.setFilter("all");
            adapter.setShowCheckboxes(false);
            updateFilterUI("all");
        });
        filterThemed.setOnClickListener(v -> {
            adapter.setFilter("themed");
            adapter.setShowCheckboxes(false);
            updateFilterUI("themed");
        });
        filterUnthemed.setOnClickListener(v -> {
            adapter.setFilter("unthemed");
            adapter.setShowCheckboxes(true);
            updateFilterUI("unthemed");
        });
        if (filterSelected != null) {
            filterSelected.setOnClickListener(v -> {
                adapter.setFilter("selected");
                adapter.setShowCheckboxes(true);
                updateFilterUI("selected");
            });
        }

        // Bottom bar buttons (always visible)
        view.findViewById(R.id.btn_select_all).setOnClickListener(v -> selectAll());
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportList());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareList());

        updateBottomBar();
        return view;
    }

    private void updateStats() {
        int total = allApps.size();
        int themed = 0;
        for (AppInfo a : allApps) if (a.isThemed) themed++;
        int unthemed = total - themed;
        statTotal.setText(String.valueOf(total));
        statThemed.setText(String.valueOf(themed));
        statUnthemed.setText(String.valueOf(unthemed));
        if (total > 0) { progressBar.setMax(total); progressBar.setProgress(themed); }
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
        if (filterSelected != null) {
            filterSelected.setTextColor(active.equals("selected") ? selColor : unselColor);
            filterSelected.setBackgroundResource(active.equals("selected") ? selBg : unselBg);
        }
        updateBottomBar();
    }

    private void updateBottomBar() {
        if (adapter == null) return;
        List<AppInfo> sel = adapter.getSelectedApps();
        if (sel.isEmpty()) {
            selectedCountView.setText("选择图标申请");
        } else {
            selectedCountView.setText("已选 " + sel.size() + " 个");
        }
    }

    private void selectAll() {
        if (adapter == null) return;
        int count = 0;
        for (AppInfo a : allApps) {
            if (!a.isThemed) { a.isSelected = true; count++; }
        }
        adapter.notifyDataChanged();
        updateBottomBar();
        if (getContext() != null)
            Toast.makeText(getContext(), "全选 " + count + " 个未适配", Toast.LENGTH_SHORT).show();
    }

    private List<AppInfo> getSelectedMissing() {
        List<AppInfo> sel = new ArrayList<>();
        for (AppInfo a : allApps) if (!a.isThemed && a.isSelected) sel.add(a);
        // If nothing explicitly selected, use all unthemed
        if (sel.isEmpty()) for (AppInfo a : allApps) if (!a.isThemed) sel.add(a);
        return sel;
    }

    private String buildRequestText(List<AppInfo> apps) {
        StringBuilder sb = new StringBuilder();
        sb.append("图标适配申请\n\n");
        for (AppInfo a : apps) {
            sb.append("应用名称：").append(a.appName).append("\n");
            sb.append("包名：").append(a.packageName).append("\n");
            sb.append("Activity：").append(a.componentName).append("\n");
            sb.append("Component：").append(a.getComponentInfo()).append("\n\n");
        }
        sb.append("共 ").append(apps.size()).append(" 个应用\n");
        return sb.toString();
    }

    private void exportList() {
        if (adapter == null || getContext() == null) return;
        String text = buildRequestText(getSelectedMissing());
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Icon Request", text));
        Toast.makeText(getContext(), "已复制申请列表", Toast.LENGTH_SHORT).show();
    }

    private void shareList() {
        if (adapter == null || getContext() == null) return;
        String text = buildRequestText(getSelectedMissing());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Icon Request");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "分享图标适配申请"));
    }
}
