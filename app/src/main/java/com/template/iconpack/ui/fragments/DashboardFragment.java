package com.template.iconpack.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
        int themed = 0;
        for (AppInfo a : apps) if (a.isThemed) themed++;

        int iconCount = icons.size();
        int wpCount = wps.size();
        int appCount = apps.size();
        int missing = appCount - themed;

        setupHero(iconCount, wpCount, themed);
        setupStats(iconCount, appCount, themed, missing);
        setupQuickEntries(iconCount, themed, appCount, wpCount);
        setupScroll();

        return rootView;
    }

    private void setupBrand(Context ctx) {
        try {
            ((TextView) rootView.findViewById(R.id.hero_app_name))
                    .setText(getString(R.string.app_name));
            String v = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0).versionName;
            ((TextView) rootView.findViewById(R.id.hero_version)).setText("v" + v);
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

    private void setupStats(int icons, int apps, int themed, int missing) {
        t(R.id.stat_icon_count, String.valueOf(icons));
        t(R.id.stat_app_count, String.valueOf(apps));
        t(R.id.stat_themed_count, String.valueOf(themed));
        t(R.id.stat_missing_count, String.valueOf(missing));
    }

    private void setupQuickEntries(int icons, int themed, int apps, int wp) {
        t(R.id.quick_icons_subtitle, icons + " 个图标");
        t(R.id.quick_request_subtitle, themed + " / " + apps + " 已适配");
        t(R.id.quick_wallpaper_subtitle, wp + " 张云端壁纸");

        click(R.id.entry_browse, 11);    // NAV_ICONS
        click(R.id.entry_request, 12);   // NAV_REQUEST
        click(R.id.entry_wallpaper, 13); // NAV_WALLPAPERS
    }

    private void t(int id, String value) {
        View v = rootView.findViewById(id);
        if (v instanceof TextView) ((TextView) v).setText(value);
    }

    private void click(int id, int pos) {
        View v = rootView.findViewById(id);
        if (v != null) v.setOnClickListener(vv -> nav(pos));
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
