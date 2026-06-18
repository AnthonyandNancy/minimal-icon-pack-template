package com.template.iconpack.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    public static final int TARGET_ICONS = 11;
    public static final int TARGET_REQUEST_ALL = 12;
    public static final int TARGET_WALLPAPERS = 13;
    public static final int TARGET_REQUEST_THEMED = 14;
    public static final int TARGET_REQUEST_UNTHEMED = 15;

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
        setupOverviewNavigation();
        buildFindMeCards(ctx);
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

    private void setupOverviewNavigation() {
        bindNavigationTarget(R.id.hero_stat_icons, TARGET_ICONS);
        bindNavigationTarget(R.id.hero_stat_wallpapers, TARGET_WALLPAPERS);
        bindNavigationTarget(R.id.hero_stat_themed, TARGET_REQUEST_THEMED);
        bindNavigationTarget(R.id.stat_card_icons, TARGET_ICONS);
        bindNavigationTarget(R.id.stat_card_apps, TARGET_REQUEST_ALL);
        bindNavigationTarget(R.id.stat_card_themed, TARGET_REQUEST_THEMED);
        bindNavigationTarget(R.id.stat_card_missing, TARGET_REQUEST_UNTHEMED);
    }

    private void bindNavigationTarget(int viewId, int target) {
        View view = rootView.findViewById(viewId);
        if (view == null) return;
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnClickListener(v -> nav(target));
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

    private void buildFindMeCards(Context ctx) {
        ViewGroup c = rootView.findViewById(R.id.dashboard_entries);
        if (c == null) return;
        c.removeAllViews();

        FindMeItem[] items = loadFindMeItems(ctx);
        View header = rootView.findViewById(R.id.find_me_section_header);
        if (items.length == 0) {
            if (header != null) header.setVisibility(View.GONE);
            c.setVisibility(View.GONE);
            return;
        }
        if (header != null) header.setVisibility(View.VISIBLE);
        c.setVisibility(View.VISIBLE);

        int primary = ContextCompat.getColor(ctx, R.color.primary);
        int accent = ContextCompat.getColor(ctx, R.color.accent);

        for (FindMeItem item : items) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_entry, c, false);

            FrameLayout badge = v.findViewById(R.id.entry_badge);
            if (badge != null) badge.setBackgroundResource("image".equals(item.type) ? R.drawable.bg_badge_purple : R.drawable.bg_badge_blue);

            ImageView iv = v.findViewById(R.id.entry_icon);
            if (iv != null) {
                iv.setImageResource(resolveFindMeIcon(item.icon));
                iv.setColorFilter("image".equals(item.type) ? accent : primary);
            }

            ((TextView) v.findViewById(R.id.entry_title)).setText(item.title);
            ((TextView) v.findViewById(R.id.entry_subtitle)).setText(item.description);
            v.setOnClickListener(vv -> handleFindMeClick(ctx, item));

            c.addView(v);
        }
    }

    private FindMeItem[] loadFindMeItems(Context ctx) {
        try {
            String[] titles = ctx.getResources().getStringArray(R.array.find_me_titles);
            String[] descriptions = ctx.getResources().getStringArray(R.array.find_me_descriptions);
            String[] icons = ctx.getResources().getStringArray(R.array.find_me_icons);
            String[] types = ctx.getResources().getStringArray(R.array.find_me_types);
            String[] links = ctx.getResources().getStringArray(R.array.find_me_links);
            String[] images = ctx.getResources().getStringArray(R.array.find_me_images);
            int count = minLength(titles, descriptions, icons, types, links, images);
            FindMeItem[] result = new FindMeItem[count];
            for (int i = 0; i < count; i++) {
                result[i] = new FindMeItem(
                        titles[i],
                        descriptions[i],
                        icons[i],
                        types[i],
                        links[i],
                        images[i]
                );
            }
            return result;
        } catch (Exception ignored) {
            return new FindMeItem[0];
        }
    }

    private int minLength(String[]... arrays) {
        int min = Integer.MAX_VALUE;
        for (String[] array : arrays) {
            if (array == null) return 0;
            min = Math.min(min, array.length);
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    private void handleFindMeClick(Context ctx, FindMeItem item) {
        if ("image".equals(item.type)) {
            int imageRes = resolveDrawableByName(ctx, item.image);
            if (imageRes == 0) {
                Toast.makeText(ctx, "图片未配置", Toast.LENGTH_SHORT).show();
                return;
            }
            showFindMeImageDialog(ctx, item.title, imageRes);
            return;
        }

        String normalizedLink = normalizeFindMeLink(item.link);
        if (normalizedLink.isEmpty()) {
            Toast.makeText(ctx, "链接未配置", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(normalizedLink));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(ctx, "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }

    private String normalizeFindMeLink(String link) {
        String value = link == null ? "" : link.trim();
        if (value.isEmpty()) return "";
        if (value.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) return value;
        return "https://" + value;
    }

    private void showFindMeImageDialog(Context ctx, String title, int imageRes) {
        int padding = (int) (24 * ctx.getResources().getDisplayMetrics().density);
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(padding, padding, padding, 0);

        ImageView image = new ImageView(ctx);
        image.setImageResource(imageRes);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        box.addView(image, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView hint = new TextView(ctx);
        hint.setText("长按或截图后扫码支持");
        hint.setTextColor(ContextCompat.getColor(ctx, R.color.color_text_secondary));
        hint.setTextSize(14);
        hint.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hintParams.topMargin = padding / 2;
        box.addView(hint, hintParams);

        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setView(box)
                .setPositiveButton("关闭", null)
                .show();
    }

    private int resolveFindMeIcon(String icon) {
        String key = icon == null ? "" : icon.trim().toLowerCase();
        switch (key) {
            case "rednote":
                return R.drawable.ic_rate;
            case "douyin":
                return R.drawable.ic_share;
            case "qq":
                return R.drawable.ic_info;
            case "wechat":
            case "alipay":
                return R.drawable.ic_donate;
            case "oppo":
                return R.drawable.ic_cube_outline;
            case "website":
                return R.drawable.ic_more_apps;
            case "link":
            default:
                return R.drawable.ic_arrow_right;
        }
    }

    private int resolveDrawableByName(Context ctx, String name) {
        String safeName = name == null ? "" : name.trim();
        if (safeName.isEmpty()) return 0;
        return ctx.getResources().getIdentifier(safeName, "drawable", ctx.getPackageName());
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

    private static class FindMeItem {
        final String title;
        final String description;
        final String icon;
        final String type;
        final String link;
        final String image;

        FindMeItem(String title, String description, String icon, String type, String link, String image) {
            this.title = title == null ? "" : title;
            this.description = description == null ? "" : description;
            this.icon = icon == null ? "link" : icon;
            this.type = "image".equals(type) ? "image" : "link";
            this.link = link == null ? "" : link;
            this.image = image == null ? "" : image;
        }
    }
}
