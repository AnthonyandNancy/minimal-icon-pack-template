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

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
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
    private Toolbar toolbar;
    private NavigationView navView;
    private PreferencesHelper prefs;

    private static final int NAV_HOME = 0;
    private static final int NAV_APPLY = 1;
    private static final int NAV_ICONS = 2;
    private static final int NAV_REQUEST = 3;
    private static final int NAV_WALLPAPERS = 4;
    private static final int NAV_PRESETS = 5;
    private static final int NAV_SETTINGS = 6;
    private static final int NAV_FAQ = 7;
    private static final int NAV_ABOUT = 8;

    private int currentNavItem = NAV_HOME;
    private DashboardFragment dashboardFragment;
    private IconsFragment iconsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PreferencesHelper(this);
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        // ── Immersive transparent status bar + navigation bar ──
        makeStatusBarTransparent();

        setContentView(R.layout.activity_main);

        // ── Dynamic toolbar top margin = statusBarHeight + 16dp ──
        if (toolbar == null) toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            lp.topMargin = getStatusBarHeight() + dp(16);
            toolbar.setLayoutParams(lp);
        }

        toolbar.setTitle(R.string.dashboard_title);

        ImageButton btnRate = findViewById(R.id.btn_rate);
        ImageButton btnShare = findViewById(R.id.btn_share);
        ImageButton btnRefresh = findViewById(R.id.btn_refresh);

        btnRate.setOnClickListener(v -> openPlayStore());
        btnShare.setOnClickListener(v -> shareApp());
        btnRefresh.setOnClickListener(v -> refreshCurrentPage());

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set drawer scrim opacity
        drawer.setScrimColor(0x33000000); // ~20% black

        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        View headerView = navView.getHeaderView(0);
        TextView navAppName = headerView.findViewById(R.id.nav_app_name);
        TextView navVersion = headerView.findViewById(R.id.nav_version);
        navAppName.setText(R.string.app_name);

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
    // Immersive status bar
    // ═══════════════════════════════════════════════════════
    private void makeStatusBarTransparent() {
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        }

        View decor = window.getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }

        decor.setSystemUiVisibility(flags);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) result = getResources().getDimensionPixelSize(resId);
        return result;
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // ═══════════════════════════════════════════════════════
    // Fragment navigation (unchanged logic)
    // ═══════════════════════════════════════════════════════
    private void showFragment(int navId) {
        currentNavItem = navId;
        // Reset toolbar position
        if (toolbar != null) {
            toolbar.setTranslationY(0f);
            toolbar.setAlpha(1f);
        }
        Fragment fragment = null;

        switch (navId) {
            case NAV_HOME:
                toolbar.setTitle(R.string.dashboard_title);
                if (dashboardFragment == null) {
                    dashboardFragment = new DashboardFragment();
                    dashboardFragment.setCallback(this::onDashboardCardClicked);
                }
                fragment = dashboardFragment;
                break;
            case NAV_APPLY:
                toolbar.setTitle(R.string.apply_title);
                fragment = new ApplyFragment();
                break;
            case NAV_ICONS:
                toolbar.setTitle(R.string.icons_title);
                if (iconsFragment == null) iconsFragment = new IconsFragment();
                fragment = iconsFragment;
                break;
            case NAV_REQUEST:
                toolbar.setTitle(R.string.request_title);
                fragment = new RequestFragment();
                break;
            case NAV_WALLPAPERS:
                toolbar.setTitle(R.string.wallpapers_title);
                fragment = new WallpapersFragment();
                break;
            case NAV_PRESETS:
                toolbar.setTitle(R.string.presets_title);
                fragment = new PresetsFragment();
                break;
            case NAV_SETTINGS:
                toolbar.setTitle(R.string.settings_title);
                SettingsFragment sf = new SettingsFragment();
                sf.setCallback(() -> { iconsFragment = null; dashboardFragment = null; });
                fragment = sf;
                break;
            case NAV_FAQ:
                toolbar.setTitle(R.string.faq_title);
                fragment = new FaqFragment();
                break;
            case NAV_ABOUT:
                toolbar.setTitle(R.string.about_title);
                fragment = new AboutFragment();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
        }
    }

    private void onDashboardCardClicked(int position) {
        if (position == -1) { openPlayStore(); return; }
        if (position == -2) { shareApp(); return; }
        if (position == -3) { refreshCurrentPage(); return; }

        int entryIdx = position - 10;
        if (entryIdx >= 0 && entryIdx <= 7) {
            switch (entryIdx) {
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

    private void refreshCurrentPage() {
        if (currentNavItem == NAV_ICONS && iconsFragment != null) {
            iconsFragment.refresh();
        } else {
            dashboardFragment = null;
            iconsFragment = null;
            showFragment(currentNavItem);
        }
        Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show();
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            } catch (Exception ignored) {
                Toast.makeText(this, "无法打开应用商店", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message));
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
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

    // ── Dashboard scroll → toolbar animation ──
    @Override
    public void onScroll(int scrollY) {
        if (toolbar == null) return;
        int maxScroll = getStatusBarHeight() + dp(72);
        float fraction = Math.min(1f, (float) scrollY / maxScroll);
        toolbar.setTranslationY(-fraction * maxScroll);
        toolbar.setAlpha(1f - fraction);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (currentNavItem != NAV_HOME) {
            showFragment(NAV_HOME);
            navView.setCheckedItem(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}
