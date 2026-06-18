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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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

    public static final String FILTER_ALL = "all";
    public static final String FILTER_THEMED = "themed";
    public static final String FILTER_UNTHEMED = "unthemed";
    private static final String ARG_INITIAL_FILTER = "initial_filter";

    private RecyclerView requestList;
    private View bottomBar, btnSelectAll, btnExport, btnShare, loadingOverlay;
    private TextView selectedCountText;
    private RequestAppAdapter adapter;
    private List<AppInfo> allApps;
    private String currentFilter = FILTER_ALL;
    private boolean emailInProgress = false;

    private View pillAll, pillThemed, pillUnthemed;

    public static RequestFragment newInstance(String initialFilter) {
        RequestFragment fragment = new RequestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_FILTER, normalizeFilter(initialFilter));
        fragment.setArguments(args);
        return fragment;
    }

    public void setInitialFilter(String filter) {
        currentFilter = normalizeFilter(filter);
        if (adapter != null) {
            applyFilter(currentFilter);
        }
    }

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

        pillAll.setOnClickListener(v -> applyFilter(FILTER_ALL));
        pillThemed.setOnClickListener(v -> applyFilter(FILTER_THEMED));
        pillUnthemed.setOnClickListener(v -> applyFilter(FILTER_UNTHEMED));

        bottomBar = view.findViewById(R.id.request_bottom_bar);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        btnExport = view.findViewById(R.id.btn_export);
        btnShare = view.findViewById(R.id.btn_share);
        loadingOverlay = view.findViewById(R.id.request_loading_overlay);
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
        btnExport.setOnClickListener(v -> copyRequestList());
        btnShare.setOnClickListener(v -> sendEmail());

        updateBottomBar();
        Bundle args = getArguments();
        if (args != null) {
            currentFilter = normalizeFilter(args.getString(ARG_INITIAL_FILTER));
        }
        applyFilter(currentFilter);
        return view;
    }

    private void applyFilter(String f) {
        currentFilter = normalizeFilter(f);
        adapter.setFilter(currentFilter);
        updatePillState(currentFilter);
        updateBottomBar();
    }

    private void updatePillState(String f) {
        View[] pills = {pillAll, pillThemed, pillUnthemed};
        String[] keys = {FILTER_ALL, FILTER_THEMED, FILTER_UNTHEMED};
        for (int i = 0; i < pills.length; i++) {
            View p = pills[i];
            if (p == null) continue;
            boolean sel = keys[i].equals(f);
            p.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_surface_card);
            ((TextView)p).setTextColor(ContextCompat.getColor(requireContext(),
                    sel ? R.color.text_on_primary : R.color.text_secondary));
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
        if (emailInProgress) return;

        List<AppInfo> sel = new ArrayList<>(adapter.getSelectedApps());
        Context context = getContext();
        if (context == null) return;
        if (sel.isEmpty()) {
            Toast.makeText(context, "请先选择需要申请适配的应用。", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasAvailableEmailApp(context.getPackageManager())) {
            showNoEmailAppDialog();
            return;
        }

        Context appContext = context.getApplicationContext();
        String authorEmail = getString(R.string.request_author_email).trim();
        showEmailLoading(true);

        new Thread(() -> {
            try {
                RequestPackageExporter.Result result = RequestPackageExporter.createRequestZip(appContext, sel);
                Uri uri = FileProvider.getUriForFile(
                        appContext,
                        appContext.getPackageName() + ".fileprovider",
                        result.zipFile
                );

                PackageManager pm = appContext.getPackageManager();
                String iconPackName = getString(R.string.app_name);
                String subject = iconPackName + "图标适配申请 - " + " - " + result.count + " 个应用";
                String body = buildRequestEmailBody(sel, iconPackName);
                Intent[] emailTargets = buildEmailIntents(appContext, pm, uri, authorEmail, subject, body);

                runOnUiThread(() -> {
                    showEmailLoading(false);
                    if (!isAdded()) return;

                    if (emailTargets.length == 0) {
                        showNoEmailAppDialog();
                        return;
                    }

                    Intent chooser = Intent.createChooser(emailTargets[0], "发邮件");
                    if (emailTargets.length > 1) {
                        Intent[] initialIntents = new Intent[emailTargets.length - 1];
                        System.arraycopy(emailTargets, 1, initialIntents, 0, initialIntents.length);
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents);
                    }
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        startActivity(chooser);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "发邮件失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showEmailLoading(false);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "发邮件失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void showEmailLoading(boolean show) {
        emailInProgress = show;
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (btnShare != null) {
            btnShare.setEnabled(!show);
            btnShare.setAlpha(show ? 0.6f : 1f);
        }
        if (btnExport != null) btnExport.setEnabled(!show);
        if (btnSelectAll != null) btnSelectAll.setEnabled(!show);
    }

    private boolean hasAvailableEmailApp(PackageManager pm) {
        List<Intent> emailProbes = new ArrayList<>();

        Intent mailto = new Intent(Intent.ACTION_SENDTO);
        mailto.setData(Uri.parse("mailto:"));
        emailProbes.add(mailto);

        Intent rfc822 = new Intent(Intent.ACTION_SEND);
        rfc822.setType("message/rfc822");
        emailProbes.add(rfc822);

        for (Intent probe : emailProbes) {
            if (!pm.queryIntentActivities(probe, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
                return true;
            }
        }

        Intent zip = new Intent(Intent.ACTION_SEND);
        zip.setType("application/zip");
        if (hasLikelyEmailTarget(pm, zip)) return true;

        Intent any = new Intent(Intent.ACTION_SEND);
        any.setType("*/*");
        return hasLikelyEmailTarget(pm, any);
    }

    private boolean hasLikelyEmailTarget(PackageManager pm, Intent probe) {
        List<ResolveInfo> infos = pm.queryIntentActivities(probe, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : infos) {
            if (info.activityInfo != null && isLikelyEmailApp(pm, info)) {
                return true;
            }
        }
        return false;
    }

    private void showNoEmailAppDialog() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("未检测到邮箱应用")
                .setMessage("请先安装并登录邮箱应用，例如 QQ 邮箱、Gmail 或 Outlook，然后再发送图标适配申请。也可以先点“复制”保存申请信息。")
                .setPositiveButton("知道了", null)
                .show();
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(action);
    }

    private String buildRequestEmailBody(List<AppInfo> apps, String iconPackName) {
        StringBuilder sb = new StringBuilder();
        sb.append("图标包：").append(iconPackName).append("\n\n");
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

    private static String normalizeFilter(String filter) {
        if (FILTER_THEMED.equals(filter) || FILTER_UNTHEMED.equals(filter)) return filter;
        return FILTER_ALL;
    }
}
