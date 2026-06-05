package com.template.iconpack;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationView;
import com.template.iconpack.ui.fragments.AboutFragment;
import com.template.iconpack.ui.fragments.ApplyFragment;
import com.template.iconpack.ui.fragments.DashboardFragment;
import com.template.iconpack.ui.fragments.FaqFragment;
import com.template.iconpack.ui.fragments.IconsFragment;
import com.template.iconpack.ui.fragments.PresetsFragment;
import com.template.iconpack.ui.fragments.RequestFragment;
import com.template.iconpack.ui.fragments.SettingsFragment;
import com.template.iconpack.ui.fragments.WallpapersFragment;
import com.template.iconpack.utils.PreferencesHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   DashboardFragment.ScrollListener {

    private DrawerLayout drawer;
    private NavigationView navView;
    private PreferencesHelper prefs;

    // Home toolbar (now a LinearLayout, not Toolbar)
    private ViewGroup homeToolbar;

    private static final int NAV_HOME = 0, NAV_APPLY = 1, NAV_ICONS = 2,
            NAV_REQUEST = 3, NAV_WALLPAPERS = 4, NAV_PRESETS = 5,
            NAV_SETTINGS = 6, NAV_FAQ = 7, NAV_ABOUT = 8;
    private int currentNavItem = NAV_HOME;
    private DashboardFragment dashboardFragment;
    private IconsFragment iconsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PreferencesHelper(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        // Immersive: transparent status bar, NO light icons (dark background)
        makeStatusBarTransparent();

        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        drawer.setScrimColor(0x5C000000); // ~36% black scrim

        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        View headerView = navView.getHeaderView(0);
        TextView navAppName = headerView.findViewById(R.id.nav_app_name);
        TextView navVersion = headerView.findViewById(R.id.nav_version);
        navAppName.setText(R.string.app_name);
        navAppName.setTextColor(0xFFF9FAFB);
        navVersion.setTextColor(0xFF8FA0B5);

        try {
            navVersion.setText(getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
            navVersion.setText(R.string.version_name);
        }

        showFragment(NAV_HOME);
        navView.setCheckedItem(R.id.nav_home);
    }

    // ═══════════════════════════════════════════════════════
    // Edge-to-edge: transparent status bar, light icons (LIGHT_STATUS_BAR)
    // ═══════════════════════════════════════════════════════
    private void makeStatusBarTransparent() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(0xFF0B1020);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        }
        View decor = window.getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decor.setSystemUiVisibility(flags);
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    public int getStatusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : 0;
    }

    public int dp(int d) { return (int)(d * getResources().getDisplayMetrics().density); }

    // ═══════════════════════════════════════════════════════
    // Fragment navigation
    // ═══════════════════════════════════════════════════════
    private void showFragment(int navId) {
        currentNavItem = navId;
        if (homeToolbar != null) {
            homeToolbar.setTranslationY(0f);
            homeToolbar.setAlpha(1f);
        }
        Fragment fragment = null;

        switch (navId) {
            case NAV_HOME:
                if (dashboardFragment == null) {
                    dashboardFragment = new DashboardFragment();
                    dashboardFragment.setCallback(this::onDashboardCardClicked);
                }
                fragment = dashboardFragment;
                break;
            case NAV_APPLY:
                // Wrap with back-bar: sub-page fragment needs back arrow
                fragment = new ApplyFragment();
                break;
            case NAV_ICONS:
                if (iconsFragment == null) iconsFragment = new IconsFragment();
                fragment = iconsFragment;
                break;
            case NAV_REQUEST:
                fragment = new RequestFragment();
                break;
            case NAV_WALLPAPERS:
                fragment = new WallpapersFragment();
                break;
            case NAV_PRESETS:
                fragment = new PresetsFragment();
                break;
            case NAV_SETTINGS:
                SettingsFragment sf = new SettingsFragment();
                sf.setCallback(() -> { iconsFragment = null; dashboardFragment = null; });
                fragment = sf;
                break;
            case NAV_FAQ:
                fragment = new FaqFragment();
                break;
            case NAV_ABOUT:
                fragment = new AboutFragment();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
        }
    }

    // Hook for DashboardFragment to register home toolbar
    public void registerHomeToolbar(ViewGroup tb) {
        this.homeToolbar = tb;
        View menuBtn = tb.findViewById(R.id.btn_menu_home);
        if (menuBtn != null) {
            menuBtn.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        }
    }

    private void onDashboardCardClicked(int pos) {
        int idx = pos - 10;
        if (idx >= 0 && idx <= 7) {
            switch (idx) {
                case 0: showFragment(NAV_APPLY);     navView.setCheckedItem(R.id.nav_apply);      break;
                case 1: showFragment(NAV_ICONS);     navView.setCheckedItem(R.id.nav_icons);      break;
                case 2: showFragment(NAV_REQUEST);   navView.setCheckedItem(R.id.nav_request);    break;
                case 3: showFragment(NAV_WALLPAPERS); navView.setCheckedItem(R.id.nav_wallpapers); break;
                case 4: showFragment(NAV_PRESETS);   navView.setCheckedItem(R.id.nav_presets);    break;
                case 5: showFragment(NAV_SETTINGS);  navView.setCheckedItem(R.id.nav_settings);   break;
                case 6: showFragment(NAV_FAQ);       navView.setCheckedItem(R.id.nav_faq);        break;
                case 7: showFragment(NAV_ABOUT);     navView.setCheckedItem(R.id.nav_about);      break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    public void onBackClicked(View v) { onBackPressed(); }

    // ── Scroll listener (DashboardScrollView) ──
    @Override
    public void onScroll(int scrollY) {
        if (homeToolbar == null) return;
        int max = getStatusBarHeight() + dp(72);
        float f = Math.min(1f, (float)scrollY / max);
        homeToolbar.setTranslationY(-f * max);
        homeToolbar.setAlpha(1f - f);
    }

    private void refreshCurrentPage() {
        if (currentNavItem == NAV_ICONS && iconsFragment != null) iconsFragment.refresh();
        else { dashboardFragment = null; iconsFragment = null; showFragment(currentNavItem); }
        Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show();
    }

    private void openPlayStore() {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()))); }
        catch (Exception e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()))); }
            catch (Exception ignored) { Toast.makeText(this, "无法打开应用商店", Toast.LENGTH_SHORT).show(); }
        }
    }

    private void shareApp() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message));
        startActivity(Intent.createChooser(i, getString(R.string.action_share)));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) showFragment(NAV_HOME);
        else if (id == R.id.nav_apply) showFragment(NAV_APPLY);
        else if (id == R.id.nav_icons) showFragment(NAV_ICONS);
        else if (id == R.id.nav_request) showFragment(NAV_REQUEST);
        else if (id == R.id.nav_wallpapers) showFragment(NAV_WALLPAPERS);
        else if (id == R.id.nav_presets) showFragment(NAV_PRESETS);
        else if (id == R.id.nav_settings) showFragment(NAV_SETTINGS);
        else if (id == R.id.nav_faq) showFragment(NAV_FAQ);
        else if (id == R.id.nav_about) showFragment(NAV_ABOUT);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else if (currentNavItem != NAV_HOME) { showFragment(NAV_HOME); navView.setCheckedItem(R.id.nav_home); }
        else super.onBackPressed();
    }
}
