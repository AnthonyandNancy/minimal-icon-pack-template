package com.template.iconpack.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        view.findViewById(R.id.btn_export).setOnClickListener(v -> copyRequestList());
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

    private void copyRequestList() {
        List<AppInfo> sel = adapter.getSelectedApps();
        if (sel.isEmpty()) {
            Toast.makeText(getContext(), "请先选择需要申请适配的应用。", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (AppInfo a : sel) {
            sb.append(a.appName).append(" | ")
                    .append(a.packageName).append(" | ")
                    .append(a.componentName).append("\n");
        }
        ClipboardManager clipboard = (ClipboardManager) requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("icon_request", sb.toString()));
        Toast.makeText(getContext(), "已复制申请信息", Toast.LENGTH_SHORT).show();
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

            PackageManager pm = context.getPackageManager();
            String subject = "图标适配申请 - " + result.count + " 个应用";
            String body = buildRequestEmailBody(sel);
            String authorEmail = getString(R.string.request_author_email).trim();
            Intent[] emailTargets = buildEmailIntents(context, pm, uri, authorEmail, subject, body);

            if (emailTargets.length == 0) {
                Toast.makeText(context, "未检测到可用邮件应用，请先安装邮箱应用。", Toast.LENGTH_LONG).show();
                return;
            }

            Intent chooser = Intent.createChooser(emailTargets[0], "发邮件");
            if (emailTargets.length > 1) {
                Intent[] initialIntents = new Intent[emailTargets.length - 1];
                System.arraycopy(emailTargets, 1, initialIntents, 0, initialIntents.length);
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents);
            }
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "发邮件失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String buildRequestEmailBody(List<AppInfo> apps) {
        StringBuilder sb = new StringBuilder();
        sb.append("需适配的应用列表（共 ").append(apps.size()).append(" 个）：\n\n");

        int index = 1;
        for (AppInfo app : apps) {
            sb.append(index++).append(". 应用名：").append(safeText(app.appName)).append("\n")
                    .append("   包名：").append(safeText(app.packageName)).append("\n")
                    .append("   启动项：").append(safeText(app.componentName)).append("\n\n");
        }

        return sb.toString().trim();
    }

    private Intent[] buildEmailIntents(
            Context context,
            PackageManager pm,
            Uri attachmentUri,
            String to,
            String subject,
            String body
    ) {
        List<Intent> emailProbes = new ArrayList<>();
        List<Intent> attachmentProbes = new ArrayList<>();

        Intent mailto = new Intent(Intent.ACTION_SENDTO);
        mailto.setData(Uri.parse("mailto:"));
        emailProbes.add(mailto);

        Intent rfc822 = new Intent(Intent.ACTION_SEND);
        rfc822.setType("message/rfc822");
        emailProbes.add(rfc822);

        Intent zip = new Intent(Intent.ACTION_SEND);
        zip.setType("application/zip");
        attachmentProbes.add(zip);

        Intent any = new Intent(Intent.ACTION_SEND);
        any.setType("*/*");
        attachmentProbes.add(any);

        Map<String, Intent> targets = new LinkedHashMap<>();
        List<String> emailPackages = new ArrayList<>();

        for (Intent probe : emailProbes) {
            List<ResolveInfo> infos = pm.queryIntentActivities(probe, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo info : infos) {
                if (info.activityInfo == null) continue;
                String pkg = info.activityInfo.packageName;
                if (!emailPackages.contains(pkg)) emailPackages.add(pkg);
                addEmailTarget(context, targets, info, attachmentUri, to, subject, body);
            }
        }

        for (Intent probe : attachmentProbes) {
            List<ResolveInfo> infos = pm.queryIntentActivities(probe, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo info : infos) {
                if (info.activityInfo == null) continue;
                if (!emailPackages.contains(info.activityInfo.packageName) && !isLikelyEmailApp(pm, info)) continue;
                addEmailTarget(context, targets, info, attachmentUri, to, subject, body);
            }
        }

        return targets.values().toArray(new Intent[0]);
    }

    private void addEmailTarget(
            Context context,
            Map<String, Intent> targets,
            ResolveInfo info,
            Uri attachmentUri,
            String to,
            String subject,
            String body
    ) {
        Intent target = createTargetEmailIntent(
                info,
                attachmentUri,
                to,
                subject,
                body
        );
        String key = info.activityInfo.packageName + "/" + info.activityInfo.name;
        targets.put(key, target);
        context.grantUriPermission(
                info.activityInfo.packageName,
                attachmentUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    }

    private Intent createTargetEmailIntent(
            ResolveInfo info,
            Uri attachmentUri,
            String to,
            String subject,
            String body
    ) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
        if (!to.isEmpty()) intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    private boolean isLikelyEmailApp(PackageManager pm, ResolveInfo info) {
        String pkg = safeLower(info.activityInfo.packageName);
        String name = safeLower(info.activityInfo.name);
        CharSequence labelSeq = info.loadLabel(pm);
        String label = safeLower(labelSeq == null ? "" : labelSeq.toString());
        String all = pkg + " " + name + " " + label;

        return all.contains("mail") ||
                all.contains("email") ||
                all.contains("gmail") ||
                all.contains("qqmail") ||
                all.contains("qq邮箱") ||
                all.contains("邮箱") ||
                all.contains("邮件") ||
                all.contains("网易") ||
                all.contains("outlook") ||
                all.contains("hotmail") ||
                all.contains("netease") ||
                all.contains("163") ||
                all.contains("126") ||
                all.contains("yeah") ||
                all.contains("aliyun") ||
                all.contains("sina") ||
                all.contains("189") ||
                all.contains("139") ||
                all.contains("yahoo") ||
                all.contains("zoho") ||
                all.contains("proton") ||
                all.contains("spark") ||
                all.contains("foxmail") ||
                all.contains("bluemail") ||
                all.contains("aquamail") ||
                all.contains("yandex") ||
                pkg.equals("com.google.android.gm") ||
                pkg.equals("com.tencent.androidqqmail") ||
                pkg.equals("com.microsoft.office.outlook");
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
