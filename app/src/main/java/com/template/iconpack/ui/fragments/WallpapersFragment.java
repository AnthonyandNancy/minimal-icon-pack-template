package com.template.iconpack.ui.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.WallpaperInfo;
import com.template.iconpack.ui.adapters.WallpaperAdapter;
import com.template.iconpack.utils.IconPackLoader;
import com.template.iconpack.utils.WallpaperImageLoader;

import java.util.List;

public class WallpapersFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);

        RecyclerView grid = view.findViewById(R.id.wallpapers_grid);
        TextView emptyView = view.findViewById(R.id.wallpapers_empty);

        if (getContext() == null) return view;
        TextView tv = view.findViewById(R.id.page_title);
        if (tv != null) tv.setText(getString(R.string.wallpapers_title));

        List<WallpaperInfo> wallpapers = IconPackLoader.loadWallpapers(getContext());

        if (wallpapers.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            grid.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            grid.setVisibility(View.VISIBLE);
            grid.setLayoutManager(new GridLayoutManager(getContext(), 2));

            WallpaperAdapter adapter = new WallpaperAdapter(wallpapers,
                    new WallpaperAdapter.OnWallpaperClickListener() {
                        @Override
                        public void onWallpaperClick(WallpaperInfo wallpaper) {
                            showPreviewDialog(wallpaper);
                        }

                        @Override
                        public void onApplyClick(WallpaperInfo wallpaper) {
                            Toast.makeText(getContext(),
                                    "壁纸功能开发中",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            grid.setAdapter(adapter);
        }

        return view;
    }

    private void showPreviewDialog(WallpaperInfo wallpaper) {
        if (getContext() == null) return;

        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View content = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_wallpaper_preview, null, false);

        ImageView preview = content.findViewById(R.id.wallpaper_preview_image);
        if (preview != null) {
            String previewUrl = wallpaper.downloadUrl != null && !wallpaper.downloadUrl.trim().isEmpty()
                    ? wallpaper.downloadUrl
                    : wallpaper.thumbnailUrl;
            WallpaperImageLoader.load(preview, previewUrl);
        }

        View close = content.findViewById(R.id.btn_preview_close);
        if (close != null) close.setOnClickListener(v -> dialog.dismiss());

        View apply = content.findViewById(R.id.btn_preview_apply);
        if (apply != null) {
            apply.setOnClickListener(v -> {
                Toast.makeText(getContext(), "壁纸功能开发中", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
