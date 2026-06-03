package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.WallpaperInfo;
import com.template.iconpack.ui.adapters.WallpaperAdapter;
import com.template.iconpack.utils.IconPackLoader;

import java.util.List;

public class WallpapersFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);

        RecyclerView grid = view.findViewById(R.id.wallpapers_grid);
        TextView emptyView = view.findViewById(R.id.wallpapers_empty);

        if (getContext() == null) return view;

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
                            Toast.makeText(getContext(),
                                    "预览: " + wallpaper.title + "\n(需 Electron 扩展实现完整预览)",
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onApplyClick(WallpaperInfo wallpaper) {
                            Toast.makeText(getContext(),
                                    "设置壁纸功能预留\n(需 Electron 扩展实现)",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            grid.setAdapter(adapter);
        }

        return view;
    }
}
