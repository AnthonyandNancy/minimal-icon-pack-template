package com.template.iconpack.ui.fragments;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.template.iconpack.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tv = view.findViewById(R.id.page_title);
        if (tv != null) tv.setText(getString(R.string.about_title));

        // Show actual version
        try {
            PackageInfo pi = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            ((TextView) view.findViewById(R.id.about_version)).setText("v" + pi.versionName);
        } catch (Exception ignored) {}

        view.findViewById(R.id.btn_about_share).setOnClickListener(v -> {
            shareInstalledApk();
        });

        return view;
    }

    private void shareInstalledApk() {
        Context context = getContext();
        if (context == null) return;

        try {
            File apkFile = copyInstalledApkToCache(context);
            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    apkFile
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/vnd.android.package-archive");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_STREAM, apkUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newUri(
                    context.getContentResolver(),
                    getString(R.string.app_name),
                    apkUri
            ));

            startActivity(Intent.createChooser(intent, getString(R.string.about_share)));
        } catch (Exception e) {
            Toast.makeText(context, "分享 APK 失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private File copyInstalledApkToCache(Context context) throws IOException {
        ApplicationInfo appInfo = context.getApplicationInfo();
        String sourcePath = appInfo.publicSourceDir != null ? appInfo.publicSourceDir : appInfo.sourceDir;
        File sourceApk = new File(sourcePath);
        File shareDir = new File(context.getCacheDir(), "shared_apk");
        if (!shareDir.exists() && !shareDir.mkdirs()) {
            throw new IOException("无法创建缓存目录");
        }

        File targetApk = new File(shareDir, buildSharedApkName(context));
        copyFile(sourceApk, targetApk);
        return targetApk;
    }

    private String buildSharedApkName(Context context) {
        String appName = getString(R.string.app_name)
                .replaceAll("[\\\\/:*?\"<>|\\s]+", "_")
                .replaceAll("^_+|_+$", "");
        if (appName.isEmpty()) appName = context.getPackageName();
        return appName + ".apk";
    }

    private void copyFile(File source, File target) throws IOException {
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(target, false)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}
