package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.WallpaperInfo;
import com.template.iconpack.utils.WallpaperImageLoader;

import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    public interface OnWallpaperClickListener {
        void onWallpaperClick(WallpaperInfo wallpaper);
        void onApplyClick(WallpaperInfo wallpaper);
    }

    private final List<WallpaperInfo> wallpapers;
    private final OnWallpaperClickListener listener;

    public WallpaperAdapter(List<WallpaperInfo> wallpapers, OnWallpaperClickListener listener) {
        this.wallpapers = wallpapers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wallpaper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WallpaperInfo wp = wallpapers.get(position);
        WallpaperImageLoader.load(holder.thumbnail, wp.thumbnailUrl);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWallpaperClick(wp);
        });
        holder.btnApply.setOnClickListener(v -> {
            if (listener != null) listener.onApplyClick(wp);
        });
    }

    @Override
    public int getItemCount() {
        return wallpapers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        View btnApply;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.wallpaper_thumbnail);
            btnApply = itemView.findViewById(R.id.btn_wallpaper_apply);
        }
    }
}
