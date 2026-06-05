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
        setupMenu();

        List<DrawableInfo> icons = IconPackLoader.loadDrawables(ctx);
        List<AppInfo> apps = AppScanner.scanInstalledApps(ctx);
        List<WallpaperInfo> wps = IconPackLoader.loadWallpapers(ctx);
        int themed = 0;
        for (AppInfo a : apps) if (a.isThemed) themed++;

        setupHero(ctx, icons.size(), wps.size(), themed);
        buildQuickCards(ctx, icons.size(), apps.size(), themed);
        buildEntryCards(ctx, icons.size(), apps.size(), themed, wps.size());
        setupScroll();

        return rootView;
    }

    private void setupBrand(Context ctx) {
        ((TextView) rootView.findViewById(R.id.hero_app_name)).setText(getString(R.string.app_name));
        try {
            String v = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
            ((TextView) rootView.findViewById(R.id.hero_version)).setText("v" + v);
        } catch (Exception ignored) {}
    }

    private void setupMenu() {
        View b = rootView.findViewById(R.id.btn_menu_home);
        if (b != null && getActivity() instanceof MainActivity)
            b.setOnClickListener(v -> ((MainActivity) getActivity()).openDrawer());
    }

    private void setupHero(Context ctx, int iconCount, int wpCount, int themed) {
        ((TextView) rootView.findViewById(R.id.stat_icons)).setText(String.valueOf(iconCount));
        ((TextView) rootView.findViewById(R.id.stat_wallpapers)).setText(String.valueOf(wpCount));
        ((TextView) rootView.findViewById(R.id.stat_themed)).setText(String.valueOf(themed));
    }

    private void nav(int pos) {
        if (callback != null) { callback.onCardClicked(pos); return; }
        // Fallback: call MainActivity directly
        if (getActivity() instanceof MainActivity)
            ((com.template.iconpack.MainActivity) getActivity()).onDashboardCardClicked(pos);
    }

    private void buildQuickCards(Context ctx, int icons, int apps, int themed) {
        GridLayout g = rootView.findViewById(R.id.dashboard_stats);
        g.removeAllViews();
        int unthemed = apps - themed;
        String[][] d = {{"图标",String.valueOf(icons),"已打包图标"},{"应用",String.valueOf(apps),"已安装应用"},
                {"已适配",String.valueOf(themed),"适配累计"},{"未适配",String.valueOf(unthemed),"待适配"}};
        int[] cs = {R.color.primary, R.color.text_primary, R.color.status_themed, R.color.status_unthemed};
        float dp = ctx.getResources().getDisplayMetrics().density;
        for (int i = 0; i < 4; i++) {
            View c = LayoutInflater.from(ctx).inflate(R.layout.item_dashboard_card, g, false);
            c.setBackgroundResource(R.drawable.bg_surface_card);
            ((TextView) c.findViewById(R.id.card_icon)).setText(d[i][0]);
            TextView vv = c.findViewById(R.id.card_value);
            vv.setText(d[i][1]);
            vv.setTextColor(ctx.getResources().getColor(cs[i]));
            ((TextView) c.findViewById(R.id.card_title)).setText(d[i][2]);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0; p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            p.setMargins((int)(6*dp),(int)(6*dp),(int)(6*dp),(int)(6*dp));
            c.setLayoutParams(p);
            g.addView(c);
        }
    }

    private void buildEntryCards(Context ctx, int icons, int apps, int themed, int wp) {
        LinearLayout c = rootView.findViewById(R.id.dashboard_entries);
        c.removeAllViews();
        String[] t = {"浏览图标","申请图标","壁纸"};
        String[] ds = {icons+" 个图标", themed+" / "+apps+" 已适配", wp+" 张云端壁纸"};
        int[] is = {R.drawable.ic_rate, R.drawable.ic_info, R.drawable.ic_wallpaper};
        int[] navs = {11, 12, 13};
        float dp = ctx.getResources().getDisplayMetrics().density;
        for (int i = 0; i < 3; i++) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.item_launcher, c, false);
            v.setBackgroundResource(R.drawable.bg_surface_card);
            ((TextView) v.findViewById(R.id.launcher_name)).setText(t[i]);
            ((TextView) v.findViewById(R.id.entry_desc)).setText(ds[i]);
            ImageView iv = v.findViewById(R.id.launcher_icon);
            if (iv != null) iv.setImageResource(is[i]);
            int target = navs[i];
            v.setOnClickListener(vv -> nav(target));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0,0,0,(int)(12*dp));
            v.setLayoutParams(lp);
            c.addView(v);
        }
    }

    private void setupScroll() {
        if (rootView instanceof ScrollView && getActivity() instanceof ScrollListener)
            ((ScrollView) rootView).setOnScrollChangeListener(
                    (v,sx,sy,ox,oy) -> ((ScrollListener) getActivity()).onScroll(sy));
    }
}
