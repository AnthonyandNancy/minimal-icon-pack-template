package com.template.iconpack.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;

import com.template.iconpack.R;
import com.template.iconpack.MainActivity;
import com.template.iconpack.utils.AppScanner;
import com.template.iconpack.utils.IconPackLoader;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.WallpaperInfo;

import java.util.List;

public class DashboardFragment extends Fragment {

    public interface ScrollListener { void onScroll(int sy); }
    public interface CardCallback { void onCardClicked(int pos); }

    private CardCallback callback;
    private View rootView;

    public void setCallback(CardCallback cb) { this.callback = cb; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Context ctx = getContext();
        if (ctx == null) return rootView;

        setupBrand(ctx);
        setupMenuButton();
        setupApplyButton();

        List<DrawableInfo> icons = IconPackLoader.loadDrawables(ctx);
        List<AppInfo> apps = AppScanner.scanInstalledApps(ctx);
        List<WallpaperInfo> wallpapers = IconPackLoader.loadWallpapers(ctx);
        int themedCount = 0;
        for (AppInfo a : apps) if (a.isThemed) themedCount++;

        setupStats(icons.size(), wallpapers.size());
        buildQuickCards(ctx, icons.size(), apps.size(), themedCount);
        buildEntryCards(ctx, icons.size(), apps.size(), themedCount, wallpapers.size());
        setupScrollListener();

        return rootView;
    }

    private void setupBrand(Context ctx) {
        ((TextView) rootView.findViewById(R.id.hero_app_name))
                .setText(getString(R.string.app_name));
        try {
            String v = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
            ((TextView) rootView.findViewById(R.id.hero_version)).setText("v" + v);
        } catch (Exception ignored) {}
    }

    private void setupMenuButton() {
        View btn = rootView.findViewById(R.id.btn_menu_home);
        if (btn != null && getActivity() instanceof MainActivity) {
            btn.setOnClickListener(v -> ((MainActivity) getActivity()).openDrawer());
        }
    }

    private void setupApplyButton() {
        View btn = rootView.findViewById(R.id.btn_apply);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                if (callback != null) callback.onCardClicked(10);
            });
        }
    }

    private void setupStats(int iconCount, int wallpaperCount) {
        ((TextView) rootView.findViewById(R.id.stat_icons)).setText(String.valueOf(iconCount));
        ((TextView) rootView.findViewById(R.id.stat_wallpapers)).setText(String.valueOf(wallpaperCount));
    }

    private void buildQuickCards(Context ctx, int iconCount, int totalApps, int themed) {
        GridLayout grid = rootView.findViewById(R.id.dashboard_stats);
        grid.removeAllViews();

        int unthemed = totalApps - themed;
        String[][] data = {
                {"Icons", String.valueOf(iconCount), iconCount + " icons"},
                {"Themed", String.valueOf(themed), themed + " themed"},
                {"Missing", String.valueOf(unthemed), unthemed + " pending"},
                {"Apps", String.valueOf(totalApps), totalApps + " installed"},
        };
        int[] colors = {R.color.primary, R.color.status_themed, R.color.status_unthemed, R.color.accent};

        float density = ctx.getResources().getDisplayMetrics().density;
        for (int i = 0; i < 4; i++) {
            View card = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_card, grid, false);
            card.setBackgroundResource(R.drawable.bg_card_surface);

            ((TextView) card.findViewById(R.id.card_icon)).setText(data[i][0]);
            TextView val = card.findViewById(R.id.card_value);
            val.setText(data[i][1]);
            val.setTextColor(ctx.getResources().getColor(colors[i]));
            ((TextView) card.findViewById(R.id.card_title)).setText(data[i][2]);

            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0; p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            p.setMargins((int)(6 * density), (int)(6 * density),
                    (int)(6 * density), (int)(6 * density));
            card.setLayoutParams(p);
            grid.addView(card);
        }
    }

    private void buildEntryCards(Context ctx, int iconCount, int totalApps, int themed, int wpCount) {
        LinearLayout container = rootView.findViewById(R.id.dashboard_entries);
        container.removeAllViews();

        String[] titles = {"Apply Icons", "Icon Gallery", "Request Icons", "Wallpapers"};
        String[] descs = {
                "Choose a launcher to apply",
                iconCount + " beautiful icons",
                themed + " apps themed so far",
                wpCount + " cloud wallpapers"
        };
        int[] icons = {R.drawable.ic_apply_card, R.drawable.ic_rate,
                R.drawable.ic_info, R.drawable.ic_wallpaper};

        float density = ctx.getResources().getDisplayMetrics().density;
        for (int i = 0; i < 4; i++) {
            View card = LayoutInflater.from(ctx).inflate(R.layout.item_launcher, container, false);
            card.setBackgroundResource(R.drawable.bg_card_surface);

            ((TextView) card.findViewById(R.id.launcher_name)).setText(titles[i]);
            ((TextView) card.findViewById(R.id.entry_desc)).setText(descs[i]);
            ImageView iv = card.findViewById(R.id.launcher_icon);
            if (iv != null) iv.setImageResource(icons[i]);

            int idx = i;
            card.setOnClickListener(v -> {
                if (callback != null) callback.onCardClicked(10 + idx);
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, (int)(12 * density));
            card.setLayoutParams(lp);
            container.addView(card);
        }
    }

    private void setupScrollListener() {
        if (rootView instanceof ScrollView && getActivity() instanceof ScrollListener) {
            ((ScrollView) rootView).setOnScrollChangeListener(
                    (v, sx, sy, ox, oy) -> ((ScrollListener) getActivity()).onScroll(sy));
        }
    }
}
