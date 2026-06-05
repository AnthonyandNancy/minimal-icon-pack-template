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
import com.template.iconpack.ui.anim.GlassAnimations;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Context ctx = getContext();
        if (ctx == null) return rootView;

        // Brand header
        setupBrand(ctx);

        // Menu button
        View menuBtn = rootView.findViewById(R.id.btn_menu_home);
        if (menuBtn != null && getActivity() instanceof MainActivity) {
            menuBtn.setOnClickListener(v -> ((MainActivity) getActivity()).openDrawer());
        }

        // Apply button
        View applyBtn = rootView.findViewById(R.id.btn_apply);
        if (applyBtn != null) {
            applyBtn.setOnClickListener(v -> {
                if (callback != null) callback.onCardClicked(10); // NAV_APPLY
            });
        }

        // Data
        List<DrawableInfo> icons = IconPackLoader.loadDrawables(ctx);
        List<AppInfo> apps = AppScanner.scanInstalledApps(ctx);
        List<WallpaperInfo> wallpapers = IconPackLoader.loadWallpapers(ctx);
        int themedCount = 0;
        for (AppInfo a : apps) if (a.isThemed) themedCount++;

        // Stats
        TextView statIcons = rootView.findViewById(R.id.stat_icons);
        TextView statWallpapers = rootView.findViewById(R.id.stat_wallpapers);
        if (statIcons != null) statIcons.setText(String.valueOf(icons.size()));
        if (statWallpapers != null) statWallpapers.setText(String.valueOf(wallpapers.size()));

        // Quick action cards
        buildQuickCards(ctx, icons.size(), apps.size(), themedCount, apps.size() - themedCount);

        // Feature entry cards
        buildEntryCards(ctx, icons.size(), apps.size(), themedCount, wallpapers.size());

        // Scroll listener
        if (rootView instanceof ScrollView && getActivity() instanceof ScrollListener) {
            ((ScrollView) rootView).setOnScrollChangeListener(
                    (v, sx, sy, ox, oy) -> ((ScrollListener) getActivity()).onScroll(sy));
        }
        return rootView;
    }

    private void setupBrand(Context ctx) {
        TextView nameView = rootView.findViewById(R.id.hero_app_name);
        nameView.setText(getString(R.string.app_name));
        try {
            TextView verView = rootView.findViewById(R.id.hero_version);
            verView.setText("v" + ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName);
        } catch (Exception ignored) {}
    }

    private void buildQuickCards(Context ctx, int iconCount, int totalApps, int themed, int unthemed) {
        GridLayout grid = rootView.findViewById(R.id.dashboard_stats);
        grid.removeAllViews();

        String[][] data = {
                {"Icons", String.valueOf(iconCount), String.valueOf(iconCount)},
                {"Themed", String.valueOf(themed), String.valueOf(themed)},
                {"Missing", String.valueOf(unthemed), String.valueOf(unthemed)},
                {"Apps", String.valueOf(totalApps), String.valueOf(totalApps)},
        };
        int[] colors = {R.color.primary, R.color.status_themed, R.color.status_unthemed, R.color.accent};
        int[] icons = {R.drawable.ic_rate, R.drawable.ic_rate, R.drawable.ic_rate, R.drawable.ic_rate};

        for (int i = 0; i < 4; i++) {
            View card = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_card, grid, false);
            card.setBackgroundResource(R.drawable.bg_card_surface);
            ((TextView) card.findViewById(R.id.card_icon)).setText(data[i][0]);
            ((TextView) card.findViewById(R.id.card_value)).setText(data[i][1]);
            ((TextView) card.findViewById(R.id.card_value)).setTextColor(ctx.getResources().getColor(colors[i]));
            ((TextView) card.findViewById(R.id.card_title)).setText(data[i][2]);

            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0; p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            p.setMargins(6, 6, 6, 6);
            card.setLayoutParams(p);
            grid.addView(card);
        }
    }

    private void buildEntryCards(Context ctx, int iconCount, int totalApps, int themed, int wallpaperCount) {
        LinearLayout container = rootView.findViewById(R.id.dashboard_entries);
        container.removeAllViews();

        String[] titles = {"Apply Icons", "Icon Gallery", "Request Icons", "Wallpapers"};
        String[] descs = {"Choose a launcher", iconCount + " icons", themed + " themed", wallpaperCount + " wallpapers"};

        for (int i = 0; i < 4; i++) {
            View card = LayoutInflater.from(ctx).inflate(R.layout.item_launcher, container, false);
            card.setBackgroundResource(R.drawable.bg_card_surface);
            ((TextView) card.findViewById(R.id.launcher_name)).setText(titles[i]);
            ((TextView) card.findViewById(R.id.entry_desc)).setText(descs[i]);

            int idx = i;
            card.setOnClickListener(v -> {
                if (callback != null) callback.onCardClicked(10 + idx);
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, (int)(12 * ctx.getResources().getDisplayMetrics().density));
            card.setLayoutParams(lp);
            container.addView(card);
        }
    }
}
