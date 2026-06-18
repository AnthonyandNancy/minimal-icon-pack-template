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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        buildFindMeCards(ctx, icons);
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

    private void buildFindMeCards(Context ctx, List<DrawableInfo> icons) {
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
        Map<String, Integer> packIcons = buildFindMePackIconMap(ctx, icons);

        for (FindMeItem item : items) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_entry, c, false);
            FindMeIcon icon = resolveFindMeIcon(item.icon, packIcons);

            FrameLayout badge = v.findViewById(R.id.entry_badge);
            if (badge != null) {
                if (icon.usesPackIcon) {
                    badge.setBackground(null);
                } else {
                    badge.setBackgroundResource("image".equals(item.type) ? R.drawable.bg_badge_purple : R.drawable.bg_badge_blue);
                }
            }

            ImageView iv = v.findViewById(R.id.entry_icon);
            if (iv != null) {
                iv.setImageResource(icon.resId);
                if (icon.usesPackIcon) {
                    iv.clearColorFilter();
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    setSquareSize(iv, R.dimen.quick_badge_size);
                } else {
                    iv.setColorFilter("image".equals(item.type) ? accent : primary);
                    iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    setSquareSize(iv, R.dimen.quick_badge_icon_size);
                }
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

    private FindMeIcon resolveFindMeIcon(String icon, Map<String, Integer> packIcons) {
        String appKey = normalizeFindMeAppKey(icon);
        if (!appKey.isEmpty()) {
            Integer packIcon = packIcons.get(appKey);
            if (packIcon != null && packIcon != 0) return new FindMeIcon(packIcon, true);
        }

        return new FindMeIcon(resolveDefaultFindMeIcon(icon), false);
    }

    private int resolveDefaultFindMeIcon(String icon) {
        String key = normalizeFindMeAppKey(icon);
        if (key.isEmpty()) key = normalizeFindMeKey(icon);
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

    private Map<String, Integer> buildFindMePackIconMap(Context ctx, List<DrawableInfo> icons) {
        Map<String, Integer> result = new HashMap<>();
        Map<String, String> appFilter = IconPackLoader.loadAppFilter(ctx);
        String[] appKeys = {"rednote", "douyin", "qq", "wechat", "alipay"};
        for (String appKey : appKeys) {
            int resId = resolvePackIconResource(ctx, icons, appFilter, appKey);
            if (resId != 0) result.put(appKey, resId);
        }
        return result;
    }

    private int resolvePackIconResource(Context ctx, List<DrawableInfo> icons,
                                        Map<String, String> appFilter, String appKey) {
        for (String candidate : getFindMeIconCandidates(appKey)) {
            int resId = resolveDrawableResource(ctx, icons, candidate);
            if (resId != 0) return resId;
        }

        if (appFilter == null || appFilter.isEmpty()) return 0;
        for (Map.Entry<String, String> entry : appFilter.entrySet()) {
            if (!matchesFindMePackage(appKey, entry.getKey())) continue;
            int resId = resolveDrawableResource(ctx, icons, entry.getValue());
            if (resId != 0) return resId;
        }
        return 0;
    }

    private String[] getFindMeIconCandidates(String appKey) {
        switch (appKey) {
            case "rednote":
                return new String[]{"rednote", "xiaohongshu", "xhs"};
            case "douyin":
                return new String[]{"douyin", "aweme", "tiktok"};
            case "qq":
                return new String[]{"qq", "tencent_qq", "mobileqq"};
            case "wechat":
                return new String[]{"wechat", "weixin", "weixin_chat"};
            case "alipay":
                return new String[]{"alipay", "zhifubao", "alipaygphone"};
            default:
                return new String[]{appKey};
        }
    }

    private boolean matchesFindMePackage(String appKey, String component) {
        String value = component == null ? "" : component.toLowerCase();
        switch (appKey) {
            case "rednote":
                return value.contains("com.xingin.xhs");
            case "douyin":
                return value.contains("com.ss.android.ugc.aweme");
            case "qq":
                return value.contains("com.tencent.mobileqq");
            case "wechat":
                return value.contains("com.tencent.mm");
            case "alipay":
                return value.contains("com.eg.android.alipaygphone");
            default:
                return false;
        }
    }

    private int resolveDrawableResource(Context ctx, List<DrawableInfo> icons, String name) {
        int resId = resolveDrawableByName(ctx, name);
        if (resId != 0) return resId;

        String safeName = name == null ? "" : name.trim();
        if (safeName.isEmpty() || icons == null) return 0;
        for (DrawableInfo icon : icons) {
            if (icon == null || icon.name == null) continue;
            if (safeName.equalsIgnoreCase(icon.name)
                    && icon.resId != 0
                    && icon.resId != android.R.drawable.ic_menu_gallery) {
                return icon.resId;
            }
        }
        return 0;
    }

    private String normalizeFindMeAppKey(String icon) {
        String key = normalizeFindMeKey(icon);
        switch (key) {
            case "rednote":
            case "xiaohongshu":
            case "xhs":
                return "rednote";
            case "douyin":
            case "aweme":
            case "tiktok":
                return "douyin";
            case "qq":
            case "mobileqq":
                return "qq";
            case "wechat":
            case "weixin":
                return "wechat";
            case "alipay":
            case "zhifubao":
                return "alipay";
            default:
                return "";
        }
    }

    private String normalizeFindMeKey(String icon) {
        return icon == null ? "" : icon.trim().toLowerCase().replace('-', '_');
    }

    private void setSquareSize(View view, int dimenResId) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) return;
        int size = view.getResources().getDimensionPixelSize(dimenResId);
        params.width = size;
        params.height = size;
        view.setLayoutParams(params);
    }

    private int resolveDrawableByName(Context ctx, String name) {
        String safeName = name == null ? "" : name.trim();
        if (safeName.isEmpty()) return 0;
        return ctx.getResources().getIdentifier(safeName, "drawable", ctx.getPackageName());
    }

    private static class FindMeIcon {
        final int resId;
        final boolean usesPackIcon;

        FindMeIcon(int resId, boolean usesPackIcon) {
            this.resId = resId;
            this.usesPackIcon = usesPackIcon;
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
