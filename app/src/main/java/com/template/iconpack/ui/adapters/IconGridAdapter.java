package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.IconCategoryData;
import com.template.iconpack.models.IconCategoryEntry;
import com.template.iconpack.ui.anim.GlassAnimations;

import java.util.ArrayList;
import java.util.List;

public class IconGridAdapter extends RecyclerView.Adapter<IconGridAdapter.ViewHolder> {
    public static final String CATEGORY_UNCATEGORIZED = "__uncategorized__";
    public static final String CATEGORY_UNMATCHED = "__unmatched__";
    public static final String CATEGORY_MULTI = "__multi_category__";

    private List<DrawableInfo> icons;
    private List<DrawableInfo> filteredIcons;
    private boolean showName;
    private OnIconClickListener listener;
    private String currentQuery = "";
    private String currentCategoryId = "";
    private java.util.Map<String, String[]> iconCategoryMap;
    private java.util.Map<String, IconCategoryEntry> iconEntryMap;

    public interface OnIconClickListener { void onIconClick(DrawableInfo icon); }

    public IconGridAdapter(List<DrawableInfo> icons, boolean showName) {
        this.icons = icons != null ? icons : new ArrayList<>();
        this.filteredIcons = new ArrayList<>(this.icons);
        this.showName = showName;
    }

    public void setOnIconClickListener(OnIconClickListener l) { this.listener = l; }

    public void setIconCategoryMap(java.util.Map<String, String[]> map) {
        this.iconCategoryMap = map;
        applyAll();
    }

    public void setIconCategoryData(IconCategoryData data) {
        this.iconCategoryMap = data != null ? data.iconCategoryMap : null;
        this.iconEntryMap = data != null ? data.iconEntryMap : null;
        applyAll();
    }

    public void filterByCategory(String categoryId) {
        currentCategoryId = categoryId != null ? categoryId : "";
        applyAll();
    }

    public void updateData(List<DrawableInfo> newIcons) {
        this.icons = newIcons != null ? newIcons : new ArrayList<>();
        this.filteredIcons = new ArrayList<>(this.icons);
        notifyDataSetChanged();
    }

    public int filter(String query) {
        currentQuery = query != null ? query : "";
        applyAll();
        return filteredIcons.size();
    }

    public int getFilteredCount() {
        return filteredIcons.size();
    }

    private void applyAll() {
        filteredIcons.clear();
        String lowerQuery = currentQuery.toLowerCase();
        for (DrawableInfo icon : icons) {
            // Text filter
            if (!lowerQuery.isEmpty()) {
                String lt = (icon.label != null ? icon.label : icon.name).toLowerCase();
                if (!lt.contains(lowerQuery) && !icon.name.toLowerCase().contains(lowerQuery))
                    continue;
            }
            // Category filter
            if (!currentCategoryId.isEmpty()) {
                if (!matchesCategory(icon, currentCategoryId)) continue;
            }
            filteredIcons.add(icon);
        }
        notifyDataSetChanged();
    }

    private boolean matchesCategory(DrawableInfo icon, String categoryId) {
        IconCategoryEntry entry = iconEntryMap != null ? iconEntryMap.get(icon.name) : null;

        if (CATEGORY_UNCATEGORIZED.equals(categoryId)) {
            return entry != null && "uncategorized".equals(entry.matchStatus);
        }
        if (CATEGORY_UNMATCHED.equals(categoryId)) {
            return entry != null && "unmatched".equals(entry.matchStatus);
        }
        if (CATEGORY_MULTI.equals(categoryId)) {
            return entry != null && ("multi_category".equals(entry.matchStatus)
                    || (entry.categoryIds != null && entry.categoryIds.size() > 1));
        }

        String[] cats = iconCategoryMap != null ? iconCategoryMap.get(icon.name) : null;
        if (cats != null) {
            for (String c : cats) {
                if (categoryId.equals(c)) return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_grid, parent, false);
        GlassAnimations.applyPressAnimation(view);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrawableInfo icon = filteredIcons.get(position);
        try {
            holder.image.setImageResource(icon.resId);
        } catch (Exception e) {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        holder.name.setText(icon.label != null ? icon.label : icon.name);
        holder.name.setVisibility(showName ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onIconClick(icon);
        });
    }

    @Override
    public int getItemCount() {
        return filteredIcons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.icon_image);
            name = itemView.findViewById(R.id.icon_name);
        }
    }
}
