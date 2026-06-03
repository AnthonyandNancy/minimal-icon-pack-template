package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.template.iconpack.R;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.WallpaperInfo;
import com.template.iconpack.utils.AppScanner;
import com.template.iconpack.utils.IconPackLoader;

import java.util.List;

public class DashboardFragment extends Fragment {

    private static final int[] CARD_ICONS = {
            R.drawable.ic_apply_card,
            R.drawable.ic_donate,
            R.drawable.ic_icons_card,
            R.drawable.ic_adaptive,
            R.drawable.ic_request_card,
            R.drawable.ic_wallpapers_card,
            R.drawable.ic_more_apps
    };

    private static final int[] CARD_TITLES = {
            R.string.card_apply_title,
            R.string.card_donate_title,
            R.string.card_icons_title,
            R.string.card_adaptive_title,
            R.string.card_request_title,
            R.string.card_wallpapers_title,
            R.string.card_more_apps_title
    };

    private GridLayout grid;
    private DashboardCallback callback;

    public interface DashboardCallback {
        void onCardClicked(int position);
    }

    public void setCallback(DashboardCallback callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        grid = view.findViewById(R.id.dashboard_grid);
        buildCards();
        return view;
    }

    private void buildCards() {
        grid.removeAllViews();
        if (getContext() == null) return;

        // Load stats
        List<DrawableInfo> icons = IconPackLoader.loadDrawables(getContext());
        List<AppInfo> apps = AppScanner.scanInstalledApps(getContext());
        List<WallpaperInfo> wallpapers = IconPackLoader.loadWallpapers(getContext());

        int themedCount = 0;
        int unthemedCount = 0;
        for (AppInfo app : apps) {
            if (app.isThemed) themedCount++;
            else unthemedCount++;
        }

        String[] descs = new String[]{
                "应用此图标包到启动器",
                "支持图标包的开发工作",
                icons.size() + " 个图标已就绪",
                "自适应图标示例",
                "已安装: " + apps.size() + " | 已适配: " + themedCount + " | 未适配: " + unthemedCount,
                wallpapers.size() + " 张壁纸可用",
                "查看更多应用"
        };

        for (int i = 0; i < 7; i++) {
            View card = LayoutInflater.from(getContext()).inflate(R.layout.item_dashboard_card, grid, false);
            android.widget.ImageView icon = card.findViewById(R.id.card_icon);
            TextView title = card.findViewById(R.id.card_title);
            TextView desc = card.findViewById(R.id.card_desc);
            ProgressBar progress = card.findViewById(R.id.card_progress);

            icon.setImageResource(CARD_ICONS[i]);
            title.setText(CARD_TITLES[i]);
            desc.setText(descs[i]);

            // Progress bar for request card
            if (i == 4 && apps.size() > 0) {
                progress.setVisibility(View.VISIBLE);
                progress.setMax(apps.size());
                progress.setProgress(themedCount);
            }

            final int idx = i;
            card.setOnClickListener(v -> {
                if (callback != null) callback.onCardClicked(idx);
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(4, 4, 4, 4);
            card.setLayoutParams(params);

            grid.addView(card);
        }
    }
}
