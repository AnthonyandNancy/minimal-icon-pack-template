package com.template.iconpack.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.ScrollView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.template.iconpack.R;
import com.template.iconpack.MainActivity;
import com.template.iconpack.models.AppInfo;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.WallpaperInfo;
import com.template.iconpack.ui.glass.GlassMaterialFactory;
import com.template.iconpack.ui.glass.LiquidGlassDrawable;
import com.template.iconpack.ui.anim.GlassAnimations;
import com.template.iconpack.utils.AppScanner;
import com.template.iconpack.utils.IconPackLoader;

import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardCallback callback;
    private View rootView;
    private boolean animated = false;
    private static final int DP_ENTRY = 88;

    private static final int[] ENTRY_ICONS = {
            R.drawable.ic_apply_card, R.drawable.ic_icons_card,
            R.drawable.ic_request_card, R.drawable.ic_wallpapers_card,
            R.drawable.ic_presets, R.drawable.ic_settings,
            R.drawable.ic_faq, R.drawable.ic_info
    };
    private static final int[] ENTRY_TINTS = {
            R.color.primary, R.color.accent,
            R.color.status_themed, R.color.primary,
            R.color.accent, R.color.text_secondary,
            R.color.status_unthemed, R.color.primary
    };

    public interface DashboardCallback { void onCardClicked(int position); }
    public interface ScrollListener { void onScroll(int scrollY); }
    public void setCallback(DashboardCallback callback) { this.callback = callback; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Context ctx = getContext();
        if (ctx == null) return rootView;

        float density = ctx.getResources().getDisplayMetrics().density;

        // Register home toolbar with MainActivity + apply glass background
        FrameLayout tb = rootView.findViewById(R.id.toolbar);
        if (tb != null) {
            tb.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.toolbar(), density));
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).registerHomeToolbar(tb);
            }
        }

        // Set status bar spacer height
        View spacer = rootView.findViewById(R.id.status_spacer);
        if (spacer != null && getActivity() != null) {
            int sbh = ((MainActivity)getActivity()).getStatusBarHeight();
            spacer.getLayoutParams().height = sbh + ((MainActivity)getActivity()).dp(8);
            spacer.requestLayout();
        }

        // Hero card — using new GlassMaterialFactory with bg blur
        View hero = rootView.findViewById(R.id.hero_card);
        hero.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.hero(), density));
        hero.setElevation(8f);

        // Stat & entry cards have backgrounds set in their XML/layout builders

        List<DrawableInfo> icons = IconPackLoader.loadDrawables(ctx);
        List<AppInfo> apps = AppScanner.scanInstalledApps(ctx);
        List<WallpaperInfo> wallpapers = IconPackLoader.loadWallpapers(ctx);

        int themedCount = 0;
        for (AppInfo a : apps) { if (a.isThemed) themedCount++; }
        int unthemedCount = apps.size() - themedCount;

        buildHeroCard(ctx, icons.size(), density);
        buildStatCards(ctx, icons.size(), apps.size(), themedCount, unthemedCount, density);
        buildEntryCards(ctx, icons.size(), apps.size(), themedCount, wallpapers.size(), density);

        // Toolbar scroll behavior
        if (rootView instanceof ScrollView && getActivity() instanceof ScrollListener) {
            ((ScrollView) rootView).setOnScrollChangeListener((v, sx, sy, ox, oy) ->
                    ((ScrollListener) getActivity()).onScroll(sy));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!animated) { animated = true; animateCards(); }
    }

    private void buildHeroCard(Context ctx, int iconCount, float density) {
        View card = rootView.findViewById(R.id.hero_card);
        card.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.hero(), density));

        TextView nameView = rootView.findViewById(R.id.hero_app_name);
        TextView versionView = rootView.findViewById(R.id.hero_version);
        TextView countView = rootView.findViewById(R.id.hero_icon_count);

        nameView.setText(getString(R.string.app_name));
        try {
            versionView.setText(ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0).versionName);
        } catch (Exception e) {
            versionView.setText(R.string.version_name);
        }
        countView.setText(iconCount + " icons");

        GlassAnimations.applyPressAnimation(card);
    }

    private void buildStatCards(Context ctx, int iconCount, int totalApps,
                                 int themedCount, int unthemedCount, float density) {
        GridLayout grid = rootView.findViewById(R.id.dashboard_stats);
        grid.removeAllViews();

        String[][] stats = {
                {"自定图标", String.valueOf(iconCount), "" + iconCount + " 个已就绪"},
                {"应用总数", String.valueOf(totalApps), "" + totalApps + " 个已安装"},
                {"已适配",   String.valueOf(themedCount), "" + themedCount + " 个已适配"},
                {"缺失图标", String.valueOf(unthemedCount), "" + unthemedCount + " 个待适配"},
        };
        int[] colors = {R.color.primary, R.color.accent, R.color.status_themed, R.color.status_unthemed};

        for (int i = 0; i < 4; i++) {
            View card = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_card, grid, false);
            ImageView icon = card.findViewById(R.id.card_icon);
            TextView title = card.findViewById(R.id.card_title);
            TextView value = card.findViewById(R.id.card_value);
            TextView desc  = card.findViewById(R.id.card_desc);

            // Apply LiquidGlassDrawable
            card.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.statCard(), density));

            icon.setColorFilter(getResources().getColor(colors[i]));
            title.setText(stats[i][0]);
            value.setText(stats[i][1]);
            value.setTextColor(getResources().getColor(colors[i]));
            desc.setText(stats[i][2]);
            desc.setVisibility(View.VISIBLE);

            GlassAnimations.applyPressAnimation(card);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(8, 6, 8, 6);
            card.setLayoutParams(params);
            grid.addView(card);
        }
    }

    private void buildEntryCards(Context ctx, int iconCount, int totalApps,
                                  int themedCount, int wallpaperCount, float density) {
        LinearLayout container = rootView.findViewById(R.id.dashboard_entries);
        container.removeAllViews();

        String[] titles = {"应用图标","图标列表","申请图标","壁纸","预设","设置","常见问题","关于"};
        String[] descs = {
                "选择启动器并应用图标包",
                "共 " + iconCount + " 个图标",
                "已适配 " + themedCount + " / " + totalApps,
                wallpaperCount + " 张云端壁纸",
                "图标预设样式",
                "外观与数据设置",
                "常见问题解答",
                "版本与许可信息"
        };

        for (int i = 0; i < titles.length; i++) {
            View card = LayoutInflater.from(ctx).inflate(R.layout.item_launcher, container, false);

            // Hide "应用图标" entry (index 0) — not deleted, just GONE
            if (i == 0) {
                card.setVisibility(View.GONE);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 0);
                card.setLayoutParams(lp);
                container.addView(card);
                continue;
            }

            // Apply LiquidGlassDrawable
            card.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.featureCard(), density));

            ImageView iconView = card.findViewById(R.id.launcher_icon);
            TextView  nameView = card.findViewById(R.id.launcher_name);
            TextView  descView = card.findViewById(R.id.entry_desc);

            iconView.setImageResource(ENTRY_ICONS[i]);
            iconView.setColorFilter(getResources().getColor(ENTRY_TINTS[i]));
            nameView.setText(titles[i]);
            descView.setText(descs[i]);

            GlassAnimations.applyPressAnimation(card);
            final int idx = i;
            card.setOnClickListener(v -> {
                if (callback != null) callback.onCardClicked(idx + 10);
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (DP_ENTRY * density));
            lp.setMargins(0, 0, 0, (int) (8 * density));
            card.setLayoutParams(lp);
            container.addView(card);
        }
    }

    private void animateCards() {
        GridLayout grid = rootView.findViewById(R.id.dashboard_stats);
        for (int i = 0; i < grid.getChildCount(); i++)
            GlassAnimations.animateCardEntrance(grid.getChildAt(i), i);
        LinearLayout entries = rootView.findViewById(R.id.dashboard_entries);
        for (int i = 0; i < entries.getChildCount(); i++)
            GlassAnimations.animateCardEntrance(entries.getChildAt(i), i + 4);
    }
}
