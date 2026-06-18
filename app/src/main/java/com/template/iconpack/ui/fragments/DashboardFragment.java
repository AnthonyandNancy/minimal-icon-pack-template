package com.template.iconpack.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
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
        setupMenu();

        List<DrawableInfo> icons = IconPackLoader.loadDrawables(ctx);
        List<AppInfo> apps = AppScanner.scanInstalledApps(ctx);
        List<WallpaperInfo> wps = IconPackLoader.loadWallpapers(ctx);

        int iconCount = icons.size();
        int wpCount = wps.size();
        int appCount = apps.size();
        int themed = 0;
        for (AppInfo a : apps) if (a.isThemed) themed++;
        int missing = appCount - themed;

        setupHero(iconCount, wpCount, themed);
        buildQuickCards(iconCount, appCount, themed, missing);
        buildEntryCards(ctx, iconCount, themed, appCount, wpCount);
        setupScroll();

        return rootView;
    }

    private void setupBrand(Context ctx) {
        try {
            t(R.id.hero_app_name, getString(R.string.app_name));
            String v = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
            t(R.id.hero_version, "v" + v);
        } catch (Exception ignored) {}
    }

    private void setupMenu() {
        View b = rootView.findViewById(R.id.btn_menu_home);
        if (b != null && getActivity() instanceof MainActivity)
            b.setOnClickListener(v -> ((MainActivity) getActivity()).openDrawer());
    }

    private void setupHero(int iconCount, int wpCount, int themed) {
        t(R.id.hero_icon_count, String.valueOf(iconCount));
        t(R.id.hero_wallpaper_count, String.valueOf(wpCount));
        t(R.id.hero_themed_count, String.valueOf(themed));
    }

    private void buildQuickCards(int icons, int apps, int themed, int missing) {
        Context ctx = rootView.getContext();
        int primary = ContextCompat.getColor(ctx, R.color.primary);
        int textPrimary = ContextCompat.getColor(ctx, R.color.text_primary);
        int textSecondary = ContextCompat.getColor(ctx, R.color.text_secondary);
        int statusThemed = ContextCompat.getColor(ctx, R.color.status_themed);
        int statusUnthemed = ContextCompat.getColor(ctx, R.color.status_unthemed);

        setupStatCard(R.id.stat_card_icons, R.drawable.bg_badge_blue,
                R.drawable.ic_grid_rounded, primary, "图标",
                String.valueOf(icons), primary, "已打包图标",
                R.id.card_badge, R.id.card_badge_icon,
                R.id.card_title_label, R.id.card_value, R.id.card_subtitle);

        setupStatCard(R.id.stat_card_apps, R.drawable.bg_badge_gray,
                R.drawable.ic_cube_outline, textSecondary, "应用",
                String.valueOf(apps), textPrimary, "已安装应用",
                R.id.card_badge2, R.id.card_badge_icon2,
                R.id.card_title_label2, R.id.card_value2, R.id.card_subtitle2);

        setupStatCard(R.id.stat_card_themed, R.drawable.bg_badge_green,
                R.drawable.ic_check_circle_outline, statusThemed, "已适配",
                String.valueOf(themed), statusThemed, "适配累计",
                R.id.card_badge3, R.id.card_badge_icon3,
                R.id.card_title_label3, R.id.card_value3, R.id.card_subtitle3);

        setupStatCard(R.id.stat_card_missing, R.drawable.bg_badge_red,
                R.drawable.ic_dashed_circle, statusUnthemed, "未适配",
                String.valueOf(missing), statusUnthemed, "待适配",
                R.id.card_badge4, R.id.card_badge_icon4,
                R.id.card_title_label4, R.id.card_value4, R.id.card_subtitle4);
    }

    private void setupStatCard(int rootId, int badgeBg, int iconRes, int iconTint,
                               String label, String value, int valueColor, String subtitle,
                               int badgeId, int badgeIconId,
                               int labelId, int valueId, int subtitleId) {
        View card = rootView.findViewById(rootId);
        if (card == null) return;

        FrameLayout badge = card.findViewById(badgeId);
        if (badge != null) badge.setBackgroundResource(badgeBg);

        ImageView badgeIcon = card.findViewById(badgeIconId);
        if (badgeIcon != null) {
            badgeIcon.setImageResource(iconRes);
            badgeIcon.setColorFilter(iconTint);
        }

        View labelView = card.findViewById(labelId);
        if (labelView instanceof TextView) ((TextView) labelView).setText(label);

        View valueView = card.findViewById(valueId);
        if (valueView instanceof TextView) {
            TextView tv = (TextView) valueView;
            tv.setText(value);
            tv.setTextColor(valueColor);
        }

        View subView = card.findViewById(subtitleId);
        if (subView instanceof TextView) ((TextView) subView).setText(subtitle);
    }

    private void buildEntryCards(Context ctx, int icons, int themed, int apps, int wp) {
        ViewGroup c = rootView.findViewById(R.id.dashboard_entries);
        if (c == null) return;
        c.removeAllViews();

        // Item data: title, subtitle, badgeBg, iconRes, iconTint, navTarget
        String[][] items = {
            {"浏览图标", icons + " 个图标", null, null, null, "11"},
            {"申请图标", themed + " / " + apps + " 已适配", null, null, null, "12"},
            {"壁纸", wp + " 张云端壁纸", null, null, null, "13"},
        };
        int[] badgeBgs = {R.drawable.bg_badge_blue, R.drawable.bg_badge_blue, R.drawable.bg_badge_purple};
        int[] iconRes = {R.drawable.ic_rate, R.drawable.ic_info, R.drawable.ic_image_mountain};
        int[] iconTints = {
                ContextCompat.getColor(ctx, R.color.primary),
                ContextCompat.getColor(ctx, R.color.primary),
                ContextCompat.getColor(ctx, R.color.accent)
        };

        for (int i = 0; i < 3; i++) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_entry, c, false);
            // DO NOT override background — XML MaterialCardView handles it

            FrameLayout badge = v.findViewById(R.id.entry_badge);
            if (badge != null) badge.setBackgroundResource(badgeBgs[i]);

            ImageView iv = v.findViewById(R.id.entry_icon);
            if (iv != null) {
                iv.setImageResource(iconRes[i]);
                iv.setColorFilter(iconTints[i]);
            }

            ((TextView) v.findViewById(R.id.entry_title)).setText(items[i][0]);
            ((TextView) v.findViewById(R.id.entry_subtitle)).setText(items[i][1]);

            int target = Integer.parseInt(items[i][5]);
            v.setOnClickListener(vv -> nav(target));

            c.addView(v);
        }
    }

    private void t(int id, String value) {
        View v = rootView.findViewById(id);
        if (v instanceof TextView) ((TextView) v).setText(value);
    }

    private void nav(int pos) {
        if (callback != null) callback.onCardClicked(pos);
    }

    private void setupScroll() {
        if (rootView instanceof NestedScrollView && getActivity() instanceof ScrollListener) {
            ((android.view.View) rootView).setOnScrollChangeListener(
                    (v, sx, sy, ox, oy) -> ((ScrollListener) getActivity()).onScroll(sy));
        }
    }
}
