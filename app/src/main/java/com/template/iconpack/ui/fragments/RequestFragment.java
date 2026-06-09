package com.template.iconpack.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.template.iconpack.R;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.ui.adapters.RequestAppAdapter;
import com.template.iconpack.utils.AppScanner;
import com.template.iconpack.utils.RequestPackageExporter;
import java.util.List;

public class RequestFragment extends Fragment {

    private RecyclerView requestList;
    private View bottomBar, btnSelectAll;
    private TextView selectedCountText;
    private RequestAppAdapter adapter;
    private List<AppInfo> allApps;
    private String currentFilter = "all";

    private View pillAll, pillThemed, pillUnthemed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        Context ctx = getContext();
        if (ctx == null) return view;

        TextView tv = view.findViewById(R.id.page_title);
        if (tv != null) tv.setText(getString(R.string.request_title));

        // Cache pill views
        pillAll = view.findViewById(R.id.filter_all);
        pillThemed = view.findViewById(R.id.filter_themed);
        pillUnthemed = view.findViewById(R.id.filter_unthemed);

        pillAll.setOnClickListener(v -> applyFilter("all"));
        pillThemed.setOnClickListener(v -> applyFilter("themed"));
        pillUnthemed.setOnClickListener(v -> applyFilter("unthemed"));

        bottomBar = view.findViewById(R.id.request_bottom_bar);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        selectedCountText = view.findViewById(R.id.selected_count_text);

        // List
        requestList = view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(ctx));

        allApps = AppScanner.scanInstalledApps(ctx);
        int total = allApps.size(), themed = 0;
        for (AppInfo a : allApps) if (a.isThemed) themed++;

        ((TextView) view.findViewById(R.id.stat_total)).setText(String.valueOf(total));
        ((TextView) view.findViewById(R.id.stat_themed)).setText(String.valueOf(themed));
        ((TextView) view.findViewById(R.id.stat_unthemed)).setText(String.valueOf(total - themed));

        adapter = new RequestAppAdapter(allApps);
        adapter.setSelectionListener(c -> updateBottomBar());
        requestList.setAdapter(adapter);

        btnSelectAll.setOnClickListener(v -> {
            if ("反选".equals(((TextView) btnSelectAll).getText().toString())) {
                adapter.deselectAll();
            } else {
                adapter.selectAllUnthemed();
            }
        });
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportZip());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> sendEmail());

        updateBottomBar();
        updatePillState("all");
        return view;
    }

    private void applyFilter(String f) {
        currentFilter = f;
        adapter.setFilter(f);
        updatePillState(f);
        updateBottomBar();
    }

    private void updatePillState(String f) {
        View[] pills = {pillAll, pillThemed, pillUnthemed};
        String[] keys = {"all","themed","unthemed"};
        for (int i = 0; i < pills.length; i++) {
            View p = pills[i];
            if (p == null) continue;
            boolean sel = keys[i].equals(f);
            p.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_surface_card);
            ((TextView)p).setTextColor(sel ? 0xFFFFFFFF : 0xFF6B7280);
        }
    }

    private void updateBottomBar() {
        boolean show = currentFilter.equals("unthemed") || currentFilter.equals("selected");
        bottomBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            int c = adapter.getSelectedCount();
            selectedCountText.setText("已选 " + c + " 个");
            boolean showSelect = currentFilter.equals("unthemed");
            btnSelectAll.setVisibility(showSelect ? View.VISIBLE : View.GONE);
            if (showSelect) {
                int unthemed = 0;
                for (AppInfo a : allApps) if (!a.isThemed) unthemed++;
                ((TextView) btnSelectAll).setText(c >= unthemed ? "反选" : "全选");
            }
        }
    }

    private void exportZip() {
        List<AppInfo> sel = adapter.getSelectedApps();
        if (sel.isEmpty()) {
            Toast.makeText(getContext(), "请先选择需要申请适配的应用。", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            RequestPackageExporter.Result result = RequestPackageExporter.createRequestZip(requireContext(), sel);
            Toast.makeText(
                    getContext(),
                    "已导出ZIP：" + result.zipFile.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "导出失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendEmail() {
        List<AppInfo> sel = adapter.getSelectedApps();
        Context context = getContext();
        if (context == null) return;
        if (sel.isEmpty()) {
            Toast.makeText(context, "请先选择需要申请适配的应用。", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            RequestPackageExporter.Result result = RequestPackageExporter.createRequestZip(context, sel);
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    result.zipFile
            );

            Intent selector = new Intent(Intent.ACTION_SENDTO);
            selector.setData(Uri.parse("mailto:"));

            Intent email = new Intent(Intent.ACTION_SEND);
            email.setSelector(selector);
            email.setType("application/zip");
            email.putExtra(Intent.EXTRA_SUBJECT, "图标适配申请 - " + result.count + " 个应用");
            email.putExtra(Intent.EXTRA_TEXT,
                    "你好，附件是图标适配申请包。\n\n" +
                            "包含内容：\n" +
                            "- request_icons.json\n" +
                            "- appfilter.xml\n" +
                            "- 原始应用图标\n\n" +
                            "请导入桌面端工具处理。");
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            PackageManager pm = context.getPackageManager();
            if (email.resolveActivity(pm) == null) {
                Toast.makeText(context, "未检测到可用邮件应用，请先导出ZIP后手动发送。", Toast.LENGTH_LONG).show();
                return;
            }

            List<ResolveInfo> targets = pm.queryIntentActivities(email, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo target : targets) {
                context.grantUriPermission(
                        target.activityInfo.packageName,
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            }

            startActivity(Intent.createChooser(email, "发邮件"));
        } catch (Exception e) {
            Toast.makeText(context, "发邮件失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
