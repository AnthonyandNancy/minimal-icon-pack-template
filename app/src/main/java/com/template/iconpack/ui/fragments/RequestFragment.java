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
    private View bottomBar, btnSelectAll;
    private TextView selectedCountText;
    private RequestAppAdapter adapter;
    private List<AppInfo> allApps;
    private String currentFilter = "all";

    private View pillAll, pillThemed, pillUnthemed, pillSelected;

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
        pillSelected = view.findViewById(R.id.filter_selected);

        pillAll.setOnClickListener(v -> applyFilter("all"));
        pillThemed.setOnClickListener(v -> applyFilter("themed"));
        pillUnthemed.setOnClickListener(v -> applyFilter("unthemed"));
        pillSelected.setOnClickListener(v -> applyFilter("selected"));

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

        btnSelectAll.setOnClickListener(v -> adapter.selectAllUnthemed());
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportList());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareList());

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
        View[] pills = {pillAll, pillThemed, pillUnthemed, pillSelected};
        String[] keys = {"all","themed","unthemed","selected"};
        for (int i = 0; i < pills.length; i++) {
            View p = pills[i];
            if (p == null) continue;
            boolean sel = keys[i].equals(f);
            p.setBackgroundColor(sel ? 0xFF6750A4 : 0xFFFEF7FF);
            ((TextView)p).setTextColor(sel ? 0xFFFFFFFF : 0xFF49454F);
        }
    }

    private void updateBottomBar() {
        boolean show = currentFilter.equals("unthemed") || currentFilter.equals("selected");
        bottomBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            int c = adapter.getSelectedCount();
            selectedCountText.setText("已选 " + c + " 个");
            btnSelectAll.setVisibility(currentFilter.equals("unthemed") ? View.VISIBLE : View.GONE);
        }
    }

    private void exportList() {
        List<AppInfo> sel = adapter.getSelectedApps();
        if (sel.isEmpty()) { Toast.makeText(getContext(),"未选择应用",Toast.LENGTH_SHORT).show(); return; }
        StringBuilder sb = new StringBuilder();
        for (AppInfo a : sel)
            sb.append(a.appName).append(" | ").append(a.packageName).append("\n");
        ((ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("icon_request", sb.toString()));
        Toast.makeText(getContext(),"已复制到剪贴板",Toast.LENGTH_SHORT).show();
    }

    private void shareList() {
        List<AppInfo> sel = adapter.getSelectedApps();
        if (sel.isEmpty()) { Toast.makeText(getContext(),"未选择应用",Toast.LENGTH_SHORT).show(); return; }
        StringBuilder sb = new StringBuilder();
        for (AppInfo a : sel)
            sb.append(a.appName).append(" | ").append(a.packageName)
                    .append(" | ").append(a.componentName).append("\n");
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_request_subject));
        i.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(i, getString(R.string.request_share)));
    }
}
