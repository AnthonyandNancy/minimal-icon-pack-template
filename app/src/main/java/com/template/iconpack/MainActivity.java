package com.template.iconpack;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
        implements NavigationView.OnNavigationItemSelectedListener {

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

    // Fragment references for refresh capability
    private DashboardFragment dashboardFragment;
    private IconsFragment iconsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load dark mode preference before super.onCreate
        prefs = new PreferencesHelper(this);
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.dashboard_title);

        // Setup toolbar actions
        ImageButton btnRate = findViewById(R.id.btn_rate);
        ImageButton btnShare = findViewById(R.id.btn_share);
        ImageButton btnRefresh = findViewById(R.id.btn_refresh);

        btnRate.setOnClickListener(v -> openPlayStore());
        btnShare.setOnClickListener(v -> shareApp());
        btnRefresh.setOnClickListener(v -> refreshCurrentPage());

        // Setup drawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        // Set nav header info
        View headerView = navView.getHeaderView(0);
        TextView navAppName = headerView.findViewById(R.id.nav_app_name);
        TextView navVersion = headerView.findViewById(R.id.nav_version);
        navAppName.setText(R.string.app_name);

        try {
            String version = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            navVersion.setText(version);
        } catch (Exception e) {
            navVersion.setText(R.string.version_name);
        }

        // Show dashboard by default
        showFragment(NAV_HOME);
        navView.setCheckedItem(R.id.nav_home);
    }

    private void showFragment(int navId) {
        currentNavItem = navId;
        Fragment fragment = null;

        switch (navId) {
            case NAV_HOME:
                toolbar.setTitle(R.string.dashboard_title);
                if (dashboardFragment == null) {
                    dashboardFragment = new DashboardFragment();
                    dashboardFragment.setCallback(position -> onDashboardCardClicked(position));
                }
                fragment = dashboardFragment;
                break;
            case NAV_APPLY:
                toolbar.setTitle(R.string.apply_title);
                fragment = new ApplyFragment();
                break;
            case NAV_ICONS:
                toolbar.setTitle(R.string.icons_title);
                if (iconsFragment == null) {
                    iconsFragment = new IconsFragment();
                }
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
                sf.setCallback(() -> {
                    // Recreate fragments on setting change
                    iconsFragment = null;
                    dashboardFragment = null;
                });
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
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
    }

    private void onDashboardCardClicked(int position) {
        // Quick actions
        if (position == -1) { openPlayStore(); return; }
        if (position == -2) { shareApp(); return; }
        if (position == -3) { refreshCurrentPage(); return; }

        // Entry cards: DashboardFragment sends index+10
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
        switch (currentNavItem) {
            case NAV_ICONS:
                if (iconsFragment != null) iconsFragment.refresh();
                break;
            default:
                // Recreate the current fragment
                dashboardFragment = null;
                iconsFragment = null;
                showFragment(currentNavItem);
                break;
        }
        Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show();
    }

    private void openPlayStore() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            // If Play Store not available, open browser
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(intent);
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
