package com.template.iconpack.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.IconCategoryData;
import com.template.iconpack.models.IconCategoryDef;
import com.template.iconpack.models.IconCategoryEntry;

import java.util.ArrayList;
import java.util.List;

public class CategoryChipAdapter extends RecyclerView.Adapter<CategoryChipAdapter.Holder> {

    private List<IconCategoryDef> categories = new ArrayList<>();
    private int selectedIndex = 0;
    private OnChipClickListener listener;

    public interface OnChipClickListener { void onChipClick(int position, String categoryId); }

    public CategoryChipAdapter() {
        // "全部" is always first pseudo-category
        IconCategoryDef all = new IconCategoryDef();
        all.id = "";
        all.name = "全部";
        all.color = "#4F7CFF";
        all.sort = 0;
        all.enabled = true;
        categories.add(all);
    }

    public void setCategories(List<IconCategoryDef> cats) {
        categories.clear();
        IconCategoryDef all = new IconCategoryDef();
        all.id = "";
        all.name = "全部";
        all.color = "#4F7CFF";
        all.sort = 0;
        all.enabled = true;
        categories.add(all);
        if (cats != null) for (IconCategoryDef c : cats) {
            if (c.enabled) categories.add(c);
        }
        selectedIndex = 0;
        notifyDataSetChanged();
    }

    public void setCategoryData(IconCategoryData data) {
        boolean hasUncategorized = false;
        boolean hasUnmatched = false;
        boolean hasMulti = false;
        if (data != null && data.icons != null) {
            for (IconCategoryEntry entry : data.icons) {
                if ("uncategorized".equals(entry.matchStatus)) hasUncategorized = true;
                if ("unmatched".equals(entry.matchStatus)) hasUnmatched = true;
                if ("multi_category".equals(entry.matchStatus)
                        || (entry.categoryIds != null && entry.categoryIds.size() > 1)) {
                    hasMulti = true;
                }
            }
        }

        setCategories(data != null ? data.categories : null);
        if (hasUncategorized) categories.add(createSpecial("未分类", IconGridAdapter.CATEGORY_UNCATEGORIZED, "#9CA3AF"));
        if (hasUnmatched) categories.add(createSpecial("未匹配", IconGridAdapter.CATEGORY_UNMATCHED, "#EF4444"));
        if (hasMulti) categories.add(createSpecial("多分类", IconGridAdapter.CATEGORY_MULTI, "#8B5CF6"));
        selectedIndex = 0;
        notifyDataSetChanged();
    }

    private IconCategoryDef createSpecial(String name, String id, String color) {
        IconCategoryDef cat = new IconCategoryDef();
        cat.id = id;
        cat.name = name;
        cat.color = color;
        cat.sort = 10000;
        cat.enabled = true;
        return cat;
    }

    public void setSelectedIndex(int idx) {
        if (idx != selectedIndex && idx >= 0 && idx < categories.size()) {
            int old = selectedIndex;
            selectedIndex = idx;
            notifyItemChanged(old);
            notifyItemChanged(selectedIndex);
        }
    }

    public void setOnChipClickListener(OnChipClickListener l) { this.listener = l; }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        IconCategoryDef cat = categories.get(pos);
        h.text.setText(cat.name);
        boolean sel = (pos == selectedIndex);
        h.itemView.setSelected(sel);
        if (sel) {
            h.text.setBackground(makeSelectedBackground(cat.color));
            h.text.setTextColor(0xFFFFFFFF);
        } else {
            h.text.setBackgroundResource(R.drawable.bg_surface_card);
            h.text.setTextColor(0xFF6B7280);
        }
        View.OnClickListener clickListener = v -> handleChipClick(h);
        h.itemView.setOnClickListener(clickListener);
        h.text.setOnClickListener(clickListener);
    }

    @Override public int getItemCount() { return categories.size(); }

    public String getSelectedCategoryId() {
        return selectedIndex >= 0 && selectedIndex < categories.size()
                ? categories.get(selectedIndex).id : "";
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView text;
        Holder(View v) { super(v); text = v.findViewById(R.id.chip_text); }
    }

    private void handleChipClick(Holder holder) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION || pos >= categories.size()) return;
        IconCategoryDef cat = categories.get(pos);
        setSelectedIndex(pos);
        if (listener != null) listener.onChipClick(pos, cat.id);
    }

    private GradientDrawable makeSelectedBackground(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(999f);
        try {
            drawable.setColor(Color.parseColor(color != null && !color.isEmpty() ? color : "#4F7CFF"));
        } catch (Exception e) {
            drawable.setColor(Color.parseColor("#4F7CFF"));
        }
        return drawable;
    }
}
