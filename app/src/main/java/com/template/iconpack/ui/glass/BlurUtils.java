package com.template.iconpack.ui.glass;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Fast box blur for real background sampling.
 * Approximates backdrop-filter: blur() on Android.
 */
public final class BlurUtils {

    private BlurUtils() {}

    /** Capture the view's parent content under this view's bounds and blur it. */
    public static Bitmap captureAndBlur(View view, float radiusDp, float density) {
        if (view == null || view.getWidth() <= 0 || view.getHeight() <= 0) return null;
        View parent = (View) view.getParent();
        if (parent == null) return null;

        int w = view.getWidth(), h = view.getHeight();
        float scale = 0.25f;
        int sw = Math.max(1, (int)(w * scale)), sh = Math.max(1, (int)(h * scale));

        // Temporarily hide this view to avoid recursive draw
        int oldVis = view.getVisibility();
        view.setVisibility(View.INVISIBLE);

        Bitmap full = Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(full);
        c.translate(-view.getLeft(), -view.getTop());
        parent.draw(c);

        view.setVisibility(oldVis);

        // Crop to view bounds
        Bitmap cropped = Bitmap.createBitmap(full,
                Math.max(0, view.getLeft()), Math.max(0, view.getTop()),
                Math.min(w, full.getWidth() - view.getLeft()),
                Math.min(h, full.getHeight() - view.getTop()));
        full.recycle();

        // Scale down
        Bitmap scaled = Bitmap.createScaledBitmap(cropped, sw, sh, true);
        if (scaled != cropped) cropped.recycle();

        // Apply box blur
        int radius = Math.max(1, (int)(radiusDp * density * scale));
        int iterations = 3;
        Bitmap blurred = boxBlur(scaled, radius, iterations);
        if (blurred != scaled) scaled.recycle();

        // Scale back up
        Bitmap result = Bitmap.createScaledBitmap(blurred, w, h, true);
        blurred.recycle();
        return result;
    }

    /** Public access for LiquidBackgroundView. Blur the bitmap in-place. */
    public static Bitmap boxBlurOnly(Bitmap src, int radius, int iterations) {
        return boxBlur(src, radius, iterations);
    }

    /** Fast iterative box blur (approximates Gaussian blur). */
    private static Bitmap boxBlur(Bitmap src, int radius, int iterations) {
        Bitmap a = src.copy(src.getConfig(), true);
        Bitmap b = src.copy(src.getConfig(), true);
        for (int i = 0; i < iterations; i++) {
            boolean even = (i % 2 == 0);
            boxBlurHorizontal(even ? b : a, even ? a : b, radius);
        }
        b.recycle();
        return a;
    }

    private static void boxBlurHorizontal(Bitmap src, Bitmap dst, int r) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        int[] out = new int[w * h];
        int div = r + r + 1;

        // Horizontal pass
        for (int y = 0; y < h; y++) {
            int rowStart = y * w;
            int rSum = 0, gSum = 0, bSum = 0, aSum = 0;
            for (int i = -r; i <= r; i++) {
                int p = pixels[rowStart + Math.min(w - 1, Math.max(0, i))];
                rSum += (p >> 16) & 0xFF;
                gSum += (p >> 8) & 0xFF;
                bSum += p & 0xFF;
                aSum += (p >> 24) & 0xFF;
            }
            for (int x = 0; x < w; x++) {
                out[rowStart + x] = (Math.min(255, aSum/div)<<24)
                        | (Math.min(255, rSum/div)<<16)
                        | (Math.min(255, gSum/div)<<8)
                        | Math.min(255, bSum/div);
                int l = Math.max(0, x - r);
                int rp = Math.min(w - 1, x + r + 1);
                int pl = pixels[rowStart + l], pr = pixels[rowStart + rp];
                rSum += ((pr>>16)&0xFF) - ((pl>>16)&0xFF);
                gSum += ((pr>>8)&0xFF) - ((pl>>8)&0xFF);
                bSum += (pr&0xFF) - (pl&0xFF);
                aSum += ((pr>>24)&0xFF) - ((pl>>24)&0xFF);
            }
        }

        // Vertical pass
        for (int x = 0; x < w; x++) {
            int rSum = 0, gSum = 0, bSum = 0, aSum = 0;
            for (int i = -r; i <= r; i++) {
                int idx = Math.min((h-1)*w, Math.max(0, i*w)) + x;
                int p = out[idx];
                rSum += (p >> 16) & 0xFF;
                gSum += (p >> 8) & 0xFF;
                bSum += p & 0xFF;
                aSum += (p >> 24) & 0xFF;
            }
            for (int y = 0; y < h; y++) {
                int idx = y * w + x;
                int c = (Math.min(255, aSum/div)<<24)
                        | (Math.min(255, rSum/div)<<16)
                        | (Math.min(255, gSum/div)<<8)
                        | Math.min(255, bSum/div);
                pixels[idx] = c;
                int t = Math.max(0, y - r);
                int b = Math.min(h - 1, y + r + 1);
                int pt = out[t*w + x], pb = out[b*w + x];
                rSum += ((pb>>16)&0xFF) - ((pt>>16)&0xFF);
                gSum += ((pb>>8)&0xFF) - ((pt>>8)&0xFF);
                bSum += (pb&0xFF) - (pt&0xFF);
                aSum += ((pb>>24)&0xFF) - ((pt>>24)&0xFF);
            }
        }

        dst.setPixels(pixels, 0, w, 0, 0, w, h);
    }
}
