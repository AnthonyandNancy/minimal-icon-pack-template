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
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.MainActivity;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.ui.glass.GlassMaterialFactory;
import com.template.iconpack.ui.glass.LiquidGlassDrawable;
import com.template.iconpack.ui.adapters.RequestAppAdapter;
import com.template.iconpack.utils.AppScanner;

import java.util.ArrayList;
import java.util.List;

public class RequestFragment extends Fragment implements RequestAppAdapter.FilterClickListener {

    private RecyclerView requestList;
    private View bottomBar;
    private View btnSelectAll;
    private RequestAppAdapter adapter;
    private List<AppInfo> allApps;
    private String currentFilter = "all";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        Context ctx = getContext();
        if (ctx == null) return view;

        float density = ctx.getResources().getDisplayMetrics().density;
        bottomBar = view.findViewById(R.id.request_bottom_bar);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        bottomBar.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.bottomBar(), density));

        requestList = view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(ctx));

        ImageButton btnBack = view.findViewById(R.id.btn_back_req);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        View spacer = view.findViewById(R.id.status_spacer_req);
        if (spacer != null && getActivity() instanceof MainActivity) {
            MainActivity ma = (MainActivity) getActivity();
            spacer.getLayoutParams().height = ma.getStatusBarHeight() + ma.dp(8);
            spacer.requestLayout();
        }

        allApps = AppScanner.scanInstalledApps(ctx);
        int total = allApps.size(), themed = 0;
        for (AppInfo a : allApps) if (a.isThemed) themed++;

        adapter = new RequestAppAdapter(allApps);
        adapter.setStats(total, themed, total - themed);
        adapter.setFilterListener(this);
        adapter.setAppClickListener((app, pos) -> {});
        requestList.setAdapter(adapter);

        btnSelectAll.setOnClickListener(v -> selectAll());
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportList());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareList());

        return view;
    }

    @Override
    public void onFilterClicked(String filter) {
        currentFilter = filter;
        btnSelectAll.setVisibility(filter.equals("unthemed") ? View.VISIBLE : View.GONE);
    }

    private void selectAll() {
        if (adapter == null) return;
        int count = 0;
        for (AppInfo a : allApps) {
            if (!a.isThemed) { a.isSelected = true; count++; }
        }
        adapter.notifyDataChanged();
        if (getContext() != null)
            Toast.makeText(getContext(), "全选 " + count + " 个未适配", Toast.LENGTH_SHORT).show();
    }

    private List<AppInfo> getSelectedMissing() {
        List<AppInfo> sel = new ArrayList<>();
        for (AppInfo a : allApps) if (!a.isThemed && a.isSelected) sel.add(a);
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
