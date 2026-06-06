package com.template.iconpack.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.template.iconpack.R;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallpaperImageLoader {
    public interface BitmapCallback {
        void onLoaded(Bitmap bitmap);
        void onError();
    }

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static void load(ImageView target, String url) {
        if (target == null) return;
        if (url == null || url.trim().isEmpty()) {
            target.setTag(null);
            target.setImageResource(R.drawable.preset_preview_bg);
            return;
        }

        String cleanUrl = url.trim();
        target.setTag(cleanUrl);
        target.setImageResource(R.drawable.preset_preview_bg);

        loadBitmap(target.getContext(), cleanUrl, new BitmapCallback() {
            @Override
            public void onLoaded(Bitmap bitmap) {
                Object tag = target.getTag();
                if (cleanUrl.equals(tag)) {
                    target.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onError() {
                Object tag = target.getTag();
                if (cleanUrl.equals(tag)) {
                    target.setImageResource(R.drawable.preset_preview_bg);
                }
            }
        });
    }

    public static void loadBitmap(Context context, String source, BitmapCallback callback) {
        if (callback == null) return;
        if (context == null || source == null || source.trim().isEmpty()) {
            MAIN.post(callback::onError);
            return;
        }

        String cleanSource = source.trim();
        EXECUTOR.execute(() -> {
            Bitmap bitmap = decodeSource(context.getApplicationContext(), cleanSource);
            Bitmap result = bitmap;
            MAIN.post(() -> {
                if (result != null) {
                    callback.onLoaded(result);
                } else {
                    callback.onError();
                }
            });
        });
    }

    private static Bitmap decodeSource(Context context, String source) {
        if (source.startsWith("http://") || source.startsWith("https://")) {
            return decodeRemote(source);
        }

        if (source.startsWith("file://")) {
            return BitmapFactory.decodeFile(source.substring("file://".length()));
        }
        if (source.startsWith("/") || source.matches("^[A-Za-z]:\\\\.*")) {
            return BitmapFactory.decodeFile(source);
        }

        if (source.startsWith("asset://")) {
            return decodeAsset(context, source.substring("asset://".length()));
        }
        if (source.startsWith("assets/")) {
            return decodeAsset(context, source.substring("assets/".length()));
        }

        if (source.startsWith("content://")) {
            try (InputStream input = context.getContentResolver().openInputStream(Uri.parse(source))) {
                return BitmapFactory.decodeStream(input);
            } catch (Exception ignored) {
                return null;
            }
        }

        Bitmap resource = decodeResource(context, source);
        if (resource != null) return resource;

        File file = new File(source);
        if (file.exists()) return BitmapFactory.decodeFile(file.getAbsolutePath());

        return decodeAsset(context, source);
    }

    private static Bitmap decodeRemote(String source) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(source).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setDoInput(true);
            connection.connect();
            try (InputStream input = connection.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static Bitmap decodeAsset(Context context, String path) {
        try (InputStream input = context.getAssets().open(path)) {
            return BitmapFactory.decodeStream(input);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Bitmap decodeResource(Context context, String source) {
        String name = source;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) name = name.substring(slash + 1);
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        if (name.startsWith("@drawable/")) name = name.substring("@drawable/".length());
        if (name.startsWith("@mipmap/")) name = name.substring("@mipmap/".length());
        if (name.startsWith("res://")) name = name.substring("res://".length());

        int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        if (resId == 0) {
            resId = context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
        }
        return resId != 0 ? BitmapFactory.decodeResource(context.getResources(), resId) : null;
    }
}
