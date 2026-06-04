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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.template.iconpack.R;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.WallpaperInfo;
import com.template.iconpack.ui.anim.GlassAnimations;
import com.template.iconpack.utils.AppScanner;
import com.template.iconpack.utils.IconPackLoader;

import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardCallback callback;
    private View rootView;
    private boolean animated = false;

    // Entry card definitions: icon, title, description
    private static final int[] ENTRY_ICONS = {
            R.drawable.ic_apply_card, R.drawable.ic_icons_card,
            R.drawable.ic_request_card, R.drawable.ic_wallpapers_card,
            R.drawable.ic_presets, R.drawable.ic_settings,
            R.drawable.ic_faq, R.drawable.ic_info
    };

    private static final int[] ENTRY_ICON_TINTS = {
            R.color.primary, R.color.accent,
            R.color.status_themed, R.color.primary,
            R.color.accent, R.color.text_secondary,
            R.color.status_unthemed, R.color.primary
    };

    public interface DashboardCallback {
        void onCardClicked(int position);
    }

    public void setCallback(DashboardCallback callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Context ctx = getContext();
        if (ctx == null) return rootView;

        // Load data
        List<DrawableInfo> icons = IconPackLoader.loadDrawables(ctx);
        List<AppInfo> apps = AppScanner.scanInstalledApps(ctx);
        List<WallpaperInfo> wallpapers = IconPackLoader.loadWallpapers(ctx);

        int themedCount = 0;
        for (AppInfo a : apps) { if (a.isThemed) themedCount++; }
        int unthemedCount = apps.size() - themedCount;

        // Hero card
        buildHeroCard(ctx, icons.size());

        // Quick actions
        setupQuickActions(ctx);

        // Stat cards
        buildStatCards(ctx, icons.size(), apps.size(), themedCount, unthemedCount);

        // Entry cards
        buildEntryCards(ctx, icons.size(), apps.size(), themedCount, wallpapers.size());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!animated) {
            animated = true;
            animateCards();
        }
    }

    private void buildHeroCard(Context ctx, int iconCount) {
        View card = rootView.findViewById(R.id.hero_card);
        TextView nameView = rootView.findViewById(R.id.hero_app_name);
        TextView versionView = rootView.findViewById(R.id.hero_version);
        TextView countView = rootView.findViewById(R.id.hero_icon_count);

        nameView.setText(getString(R.string.app_name));
        try {
            String version = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0).versionName;
            versionView.setText(version);
        } catch (Exception e) {
            versionView.setText(R.string.version_name);
        }
        countView.setText(iconCount + " icons");

        GlassAnimations.applyPressAnimation(card);
        GlassAnimations.animatePageEntrance(card);
    }

    private void setupQuickActions(Context ctx) {
        View btnRate = rootView.findViewById(R.id.btn_quick_rate);
        View btnShare = rootView.findViewById(R.id.btn_quick_share);
        View btnRefresh = rootView.findViewById(R.id.btn_quick_refresh);

        GlassAnimations.applyPressAnimation(btnRate);
        GlassAnimations.applyPressAnimation(btnShare);
        GlassAnimations.applyPressAnimation(btnRefresh);

        btnRate.setOnClickListener(v -> {
            if (callback != null) callback.onCardClicked(-1); // rate action
        });
        btnShare.setOnClickListener(v -> {
            if (callback != null) callback.onCardClicked(-2); // share action
        });
        btnRefresh.setOnClickListener(v -> {
            if (callback != null) callback.onCardClicked(-3); // refresh action
        });
    }

    private void buildStatCards(Context ctx, int iconCount, int totalApps,
                                 int themedCount, int unthemedCount) {
        GridLayout grid = rootView.findViewById(R.id.dashboard_stats);
        grid.removeAllViews();

        String[][] stats = {
                {"自定图标", String.valueOf(iconCount), "" + iconCount + " 个图标已就绪"},
                {"应用总数", String.valueOf(totalApps), "" + totalApps + " 个已安装"},
                {"已适配", String.valueOf(themedCount), "" + themedCount + " 个应用已适配"},
                {"缺失图标", String.valueOf(unthemedCount), "" + unthemedCount + " 个待适配"},
        };

        int[] statColors = {
                R.color.primary, R.color.accent,
                R.color.status_themed, R.color.status_unthemed
        };

        for (int i = 0; i < 4; i++) {
            View card = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_card, grid, false);
            ImageView icon = card.findViewById(R.id.card_icon);
            TextView title = card.findViewById(R.id.card_title);
            TextView value = card.findViewById(R.id.card_value);
            TextView desc = card.findViewById(R.id.card_desc);

            title.setText(stats[i][0]);
            value.setText(stats[i][1]);
            value.setTextColor(getResources().getColor(statColors[i]));
            desc.setText(stats[i][2]);
            desc.setVisibility(View.VISIBLE);

            GlassAnimations.applyPressAnimation(card);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(4, 6, 4, 6);
            params.height = (int) (118 * ctx.getResources().getDisplayMetrics().density);
            card.setLayoutParams(params);
            grid.addView(card);
        }
    }

    private void buildEntryCards(Context ctx, int iconCount, int totalApps,
                                  int themedCount, int wallpaperCount) {
        LinearLayout container = rootView.findViewById(R.id.dashboard_entries);
        container.removeAllViews();

        String[] entryTitles = {
                "应用图标", "图标列表", "申请图标", "壁纸",
                "预设", "设置", "常见问题", "关于"
        };

        String[] entryDescs = {
                "选择启动器并应用图标包",
                "共 " + iconCount + " 个图标",
                "已适配 " + themedCount + " / " + totalApps,
                wallpaperCount + " 张云端壁纸",
                "图标预设样式",
                "外观与数据设置",
                "常见问题解答",
                "版本与许可信息"
        };

        int layoutId = R.layout.item_launcher; // reuse launcher card layout for entries

        for (int i = 0; i < entryTitles.length; i++) {
            View card = LayoutInflater.from(ctx).inflate(layoutId, container, false);

            // Find views in the card
            ImageView iconView = card.findViewById(R.id.launcher_icon);
            TextView nameView = card.findViewById(R.id.launcher_name);
            TextView descView = card.findViewById(R.id.entry_desc);

            iconView.setImageResource(ENTRY_ICONS[i]);
            iconView.setColorFilter(getResources().getColor(ENTRY_ICON_TINTS[i]));
            nameView.setText(entryTitles[i]);
            descView.setText(entryDescs[i]);

            GlassAnimations.applyPressAnimation(card);
            final int idx = i;
            card.setOnClickListener(v -> {
                if (callback != null) callback.onCardClicked(idx + 1); // offset from stat cards
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (92 * ctx.getResources().getDisplayMetrics().density));
            lp.setMargins(0, 0, 0, (int) (8 * ctx.getResources().getDisplayMetrics().density));
            card.setLayoutParams(lp);
            container.addView(card);
        }
    }

    private void animateCards() {
        // Animate quick actions
        GlassAnimations.animateCardEntrance(
                rootView.findViewById(R.id.quick_actions), 1);

        // Animate stat cards
        GridLayout grid = rootView.findViewById(R.id.dashboard_stats);
        for (int i = 0; i < grid.getChildCount(); i++) {
            GlassAnimations.animateCardEntrance(grid.getChildAt(i), i + 2);
        }

        // Animate entry cards
        LinearLayout entries = rootView.findViewById(R.id.dashboard_entries);
        for (int i = 0; i < entries.getChildCount(); i++) {
            GlassAnimations.animateCardEntrance(entries.getChildAt(i), i + 6);
        }
    }
}
