package com.template.iconpack;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.template.iconpack.models.ChangelogEntry;
import com.template.iconpack.ui.fragments.AboutFragment;
import com.template.iconpack.ui.fragments.ApplyFragment;
import com.template.iconpack.ui.fragments.ChangelogFragment;
import com.template.iconpack.ui.fragments.DashboardFragment;
import com.template.iconpack.ui.fragments.FaqFragment;
import com.template.iconpack.ui.fragments.IconsFragment;
import com.template.iconpack.ui.fragments.PresetsFragment;
import com.template.iconpack.ui.fragments.RequestFragment;
import com.template.iconpack.ui.fragments.SettingsFragment;
import com.template.iconpack.ui.fragments.WallpapersFragment;
import com.template.iconpack.utils.IconPackLoader;
import com.template.iconpack.utils.PreferencesHelper;

import java.util.List;

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
            NAV_CHANGELOG = 6, NAV_SETTINGS = 7, NAV_FAQ = 8, NAV_ABOUT = 9;
    private static final int CHANGELOG_DIALOG_CONTENT_LINES = 5;
    private static final int CHANGELOG_DIALOG_CONTENT_CHARS = 160;
    private static final int CHANGELOG_DIALOG_MESSAGE_LINES = 10;
    private int currentNavItem = NAV_HOME;
    private DashboardFragment dashboardFragment;
    private IconsFragment iconsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PreferencesHelper(this);
        // Respect user dark mode preference (default: off)
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);

        configureSystemBars();

        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        drawer.setScrimColor(0x5C000000); // ~36% black scrim

        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        View headerView = navView.getHeaderView(0);
        TextView navAppName = headerView.findViewById(R.id.nav_app_name);
        TextView navVersion = headerView.findViewById(R.id.nav_version);
        navAppName.setText(R.string.app_name);
        navAppName.setTextColor(getResources().getColor(R.color.text_primary));
        navVersion.setTextColor(getResources().getColor(R.color.text_secondary));

        try {
            navVersion.setText(getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
            navVersion.setText(R.string.version_name);
        }

        applySystemBarInsets(headerView);

        showFragment(NAV_HOME);
        navView.setCheckedItem(R.id.nav_home);
        drawer.post(this::maybeShowChangelogDialog);
    }

    private void configureSystemBars() {
        Window window = getWindow();
        boolean nightMode = isNightModeActive();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.background));
        }
        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(!nightMode);
        controller.setAppearanceLightNavigationBars(!nightMode);
    }

    private boolean isNightModeActive() {
        int mode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    private void applySystemBarInsets(View headerView) {
        View contentFrame = findViewById(R.id.content_frame);
        final int contentStart = contentFrame.getPaddingStart();
        final int contentEnd = contentFrame.getPaddingEnd();
        final int headerHeight = headerView.getLayoutParams().height;
        final int headerPaddingStart = headerView.getPaddingStart();
        final int headerPaddingTop = headerView.getPaddingTop();
        final int headerPaddingEnd = headerView.getPaddingEnd();
        final int headerPaddingBottom = headerView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(drawer, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            contentFrame.setPaddingRelative(
                    contentStart,
                    bars.top,
                    contentEnd,
                    bars.bottom
            );

            navView.setPaddingRelative(bars.left, 0, bars.right, bars.bottom);

            ViewGroup.LayoutParams lp = headerView.getLayoutParams();
            lp.height = headerHeight + bars.top;
            headerView.setLayoutParams(lp);
            headerView.setPaddingRelative(
                    headerPaddingStart,
                    headerPaddingTop + bars.top,
                    headerPaddingEnd,
                    headerPaddingBottom
            );

            return windowInsets;
        });
        ViewCompat.requestApplyInsets(drawer);
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
        if (isCurrentDestination(currentFragment, navId)) {
            syncFragmentReference(navId, currentFragment);
            currentNavItem = navId;
            return;
        }

        currentNavItem = navId;
        if (homeToolbar != null) {
            homeToolbar.setTranslationY(0f);
            homeToolbar.setAlpha(1f);
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Fragment restoredFragment = fragmentManager.findFragmentById(R.id.content_frame);
            if (restoredFragment instanceof DashboardFragment) {
                dashboardFragment = (DashboardFragment) restoredFragment;
                dashboardFragment.setCallback(this::onDashboardCardClicked);
            }
            if (navId == NAV_HOME && isCurrentDestination(restoredFragment, NAV_HOME)) {
                return;
            }
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
                fragment = new ApplyFragment();
                break;
            case NAV_ICONS:
                iconsFragment = new IconsFragment();
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
            case NAV_CHANGELOG:
                fragment = new ChangelogFragment();
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
            androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.content_frame, fragment);

            if (navId != NAV_HOME) {
                transaction.addToBackStack(getNavTag(navId));
            }

            transaction.commit();
        }
    }

    private void syncFragmentReference(int navId, Fragment fragment) {
        if (navId == NAV_HOME && fragment instanceof DashboardFragment) {
            dashboardFragment = (DashboardFragment) fragment;
            dashboardFragment.setCallback(this::onDashboardCardClicked);
        } else if (navId == NAV_ICONS && fragment instanceof IconsFragment) {
            iconsFragment = (IconsFragment) fragment;
        }
    }

    private boolean isCurrentDestination(Fragment fragment, int navId) {
        if (fragment == null) return false;
        switch (navId) {
            case NAV_HOME:
                return fragment instanceof DashboardFragment;
            case NAV_APPLY:
                return fragment instanceof ApplyFragment;
            case NAV_ICONS:
                return fragment instanceof IconsFragment;
            case NAV_REQUEST:
                return fragment instanceof RequestFragment;
            case NAV_WALLPAPERS:
                return fragment instanceof WallpapersFragment;
            case NAV_PRESETS:
                return fragment instanceof PresetsFragment;
            case NAV_CHANGELOG:
                return fragment instanceof ChangelogFragment;
            case NAV_SETTINGS:
                return fragment instanceof SettingsFragment;
            case NAV_FAQ:
                return fragment instanceof FaqFragment;
            case NAV_ABOUT:
                return fragment instanceof AboutFragment;
            default:
                return false;
        }
    }

    private String getNavTag(int navId) {
        switch (navId) {
            case NAV_HOME:
                return "home";
            case NAV_APPLY:
                return "apply";
            case NAV_ICONS:
                return "icons";
            case NAV_REQUEST:
                return "request";
            case NAV_WALLPAPERS:
                return "wallpapers";
            case NAV_PRESETS:
                return "presets";
            case NAV_CHANGELOG:
                return "changelog";
            case NAV_SETTINGS:
                return "settings";
            case NAV_FAQ:
                return "faq";
            case NAV_ABOUT:
                return "about";
            default:
                return "page_" + navId;
        }
    }

    public void openDrawer() { drawer.openDrawer(GravityCompat.START); }
    public void registerHomeToolbar(ViewGroup tb) {
        this.homeToolbar = tb;
        View menuBtn = tb.findViewById(R.id.btn_menu_home);
        if (menuBtn != null) {
            menuBtn.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        }
    }

    public void onDashboardCardClicked(int pos) {
        int idx = pos - 10;
        if (idx >= 0 && idx <= 7) {
            switch (idx) {
                case 1: showFragment(NAV_ICONS);     navView.setCheckedItem(R.id.nav_icons);      break;
                case 2: showFragment(NAV_REQUEST);   navView.setCheckedItem(R.id.nav_request);    break;
                case 3: showFragment(NAV_WALLPAPERS); navView.setCheckedItem(R.id.nav_wallpapers); break;
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

    private void maybeShowChangelogDialog() {
        int currentVersionCode = getCurrentVersionCode();
        String currentVersionName = getCurrentVersionName();
        if (currentVersionCode <= 0 ||
                prefs.getLastSeenChangelogVersionCode() >= currentVersionCode) {
            return;
        }

        List<ChangelogEntry> entries = IconPackLoader.loadChangelog(this);
        if (entries.isEmpty()) return;

        ChangelogEntry current = findCurrentChangelogEntry(
                entries,
                currentVersionCode,
                currentVersionName
        );
        if (current == null) return;

        String title = TextUtils.isEmpty(current.title)
                ? getString(R.string.changelog_dialog_title)
                : current.title;
        String message = buildChangelogMessage(current);
        if (TextUtils.isEmpty(message)) {
            markChangelogSeen(currentVersionCode);
            return;
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.changelog_dialog_view_all, (clickedDialog, which) -> {
                    markChangelogSeen(currentVersionCode);
                    showFragment(NAV_CHANGELOG);
                    navView.setCheckedItem(R.id.nav_changelog);
                })
                .setPositiveButton(R.string.changelog_dialog_ok, (clickedDialog, which) -> {
                    markChangelogSeen(currentVersionCode);
                })
                .setOnDismissListener(dismissedDialog -> markChangelogSeen(currentVersionCode))
                .show();
        limitChangelogDialogMessage(dialog);
    }

    private void limitChangelogDialogMessage(AlertDialog dialog) {
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView == null) return;
        messageView.setMaxLines(CHANGELOG_DIALOG_MESSAGE_LINES);
        messageView.setEllipsize(TextUtils.TruncateAt.END);
        messageView.setVerticalScrollBarEnabled(false);
    }

    private ChangelogEntry findCurrentChangelogEntry(List<ChangelogEntry> entries,
                                                     int currentVersionCode,
                                                     String currentVersionName) {
        String normalizedCurrentVersionName = normalizeVersionName(currentVersionName);
        for (ChangelogEntry entry : entries) {
            if (entry.versionCode == currentVersionCode) {
                return entry;
            }
            if (!TextUtils.isEmpty(normalizedCurrentVersionName) &&
                    normalizedCurrentVersionName.equals(normalizeVersionName(entry.versionName))) {
                return entry;
            }
        }
        return null;
    }

    private int getCurrentVersionCode() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) Math.min(Integer.MAX_VALUE, pi.getLongVersionCode());
            }
            return pi.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCurrentVersionName() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionName != null ? pi.versionName : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String normalizeVersionName(String versionName) {
        if (TextUtils.isEmpty(versionName)) return "";
        String normalized = versionName.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            return normalized.substring(1).trim();
        }
        return normalized;
    }

    private String buildChangelogMessage(ChangelogEntry entry) {
        return buildDialogContentPreview(entry.content);
    }

    private String buildDialogContentPreview(String content) {
        if (TextUtils.isEmpty(content)) return "";

        String normalized = content.trim()
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        boolean truncated = lines.length > CHANGELOG_DIALOG_CONTENT_LINES
                || normalized.length() > CHANGELOG_DIALOG_CONTENT_CHARS;

        for (int i = 0; i < lines.length && i < CHANGELOG_DIALOG_CONTENT_LINES; i++) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(lines[i]);
            if (sb.length() >= CHANGELOG_DIALOG_CONTENT_CHARS) {
                sb.setLength(CHANGELOG_DIALOG_CONTENT_CHARS);
                truncated = true;
                break;
            }
        }

        String preview = sb.toString().trim();
        if (truncated) {
            if (!preview.isEmpty()) preview += "\n";
            preview += "... 内容较多，查看全部可继续浏览";
        }
        return preview;
    }

    private void markChangelogSeen(int versionCode) {
        prefs.setLastSeenChangelogVersionCode(versionCode);
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
        else if (id == R.id.nav_icons) showFragment(NAV_ICONS);
        else if (id == R.id.nav_request) showFragment(NAV_REQUEST);
        else if (id == R.id.nav_wallpapers) showFragment(NAV_WALLPAPERS);
        else if (id == R.id.nav_changelog) showFragment(NAV_CHANGELOG);
        else if (id == R.id.nav_settings) showFragment(NAV_SETTINGS);
        else if (id == R.id.nav_faq) showFragment(NAV_FAQ);
        else if (id == R.id.nav_about) showFragment(NAV_ABOUT);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            currentNavItem = NAV_HOME;
            iconsFragment = null;
            navView.setCheckedItem(R.id.nav_home);
        }
        else super.onBackPressed();
    }
}
