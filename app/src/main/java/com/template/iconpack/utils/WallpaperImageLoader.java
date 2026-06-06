package com.template.iconpack.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.template.iconpack.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallpaperImageLoader {
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

        EXECUTOR.execute(() -> {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(cleanUrl).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);
                connection.connect();
                try (InputStream input = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(input);
                }
            } catch (Exception ignored) {
                bitmap = null;
            } finally {
                if (connection != null) connection.disconnect();
            }

            Bitmap result = bitmap;
            MAIN.post(() -> {
                Object tag = target.getTag();
                if (cleanUrl.equals(tag)) {
                    if (result != null) {
                        target.setImageBitmap(result);
                    } else {
                        target.setImageResource(R.drawable.preset_preview_bg);
                    }
                }
            });
        });
    }
}
