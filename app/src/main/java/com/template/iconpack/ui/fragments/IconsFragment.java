package com.template.iconpack.ui.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.IconCategoryData;
import com.template.iconpack.models.IconCategoryDef;
import com.template.iconpack.models.IconCategoryEntry;
import com.template.iconpack.ui.adapters.CategoryChipAdapter;
import com.template.iconpack.ui.adapters.IconGridAdapter;
import com.template.iconpack.utils.IconPackLoader;
import com.template.iconpack.utils.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class IconsFragment extends Fragment {

    private RecyclerView iconsGrid;
    private RecyclerView categoryChips;
    private TextView emptyView;
    private TextView noResultsView;
    private EditText searchInput;
    private IconGridAdapter adapter;
    private CategoryChipAdapter chipAdapter;
    private PreferencesHelper prefs;
    private IconCategoryData categoryData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons, container, false);
        if (getContext() == null) return view;
        TextView tv = view.findViewById(R.id.page_title);
        if (tv != null) tv.setText(getString(R.string.icons_title));

        prefs = new PreferencesHelper(getContext());
        iconsGrid = view.findViewById(R.id.icons_grid);
        categoryChips = view.findViewById(R.id.icons_category_chips);
        emptyView = view.findViewById(R.id.icons_empty);
        noResultsView = view.findViewById(R.id.icons_no_results);
        searchInput = view.findViewById(R.id.icons_search);

        categoryData = IconPackLoader.loadIconPackCategories(getContext());
        List<DrawableInfo> icons = IconPackLoader.loadDrawables(getContext());

        if (icons.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            iconsGrid.setVisibility(View.GONE);
            searchInput.setVisibility(View.GONE);
            if (categoryChips != null) categoryChips.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            iconsGrid.setVisibility(View.VISIBLE);
            searchInput.setVisibility(View.VISIBLE);

            int columns = prefs.getIconColumns();
            iconsGrid.setLayoutManager(new GridLayoutManager(getContext(), columns));

            boolean showName = prefs.isShowIconName();
            adapter = new IconGridAdapter(icons, showName);
            adapter.setOnIconClickListener(icon -> showIconZoom(icon));
            iconsGrid.setAdapter(adapter);

            setupCategoryChips(view);

            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                    if (adapter != null) {
                        int count2 = adapter.filter(s.toString());
                        updateResultsVisibility(count2);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

    private void setupCategoryChips(View root) {
        if (categoryData == null) {
            if (categoryChips != null) categoryChips.setVisibility(View.GONE);
            if (adapter != null) adapter.setIconCategoryData(null);
            return;
        }
        if (categoryChips == null) return;
        categoryChips.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        chipAdapter = new CategoryChipAdapter();
        chipAdapter.setCategoryData(categoryData);
        chipAdapter.setOnChipClickListener((pos, id) -> {
            if (adapter != null) {
                adapter.filterByCategory(id);
                updateResultsVisibility(adapter.getFilteredCount());
            }
        });
        categoryChips.setAdapter(chipAdapter);
        categoryChips.setVisibility(chipAdapter.getItemCount() > 1 ? View.VISIBLE : View.GONE);

        if (adapter != null) {
            adapter.setIconCategoryData(categoryData);
        }
    }

    public void refresh() {
        if (getContext() == null) return;
        categoryData = IconPackLoader.loadIconPackCategories(getContext());
        List<DrawableInfo> icons = IconPackLoader.loadDrawables(getContext());
        if (icons.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (iconsGrid != null) iconsGrid.setVisibility(View.GONE);
            if (searchInput != null) searchInput.setVisibility(View.GONE);
            if (noResultsView != null) noResultsView.setVisibility(View.GONE);
            if (categoryChips != null) categoryChips.setVisibility(View.GONE);
        } else {
            if (emptyView != null) emptyView.setVisibility(View.GONE);
            if (searchInput != null) searchInput.setVisibility(View.VISIBLE);
            if (categoryChips != null) {
                setupCategoryChips(getView());
            }
            if (iconsGrid != null) {
                iconsGrid.setVisibility(View.VISIBLE);
                int columns = prefs.getIconColumns();
                iconsGrid.setLayoutManager(new GridLayoutManager(getContext(), columns));
                adapter = new IconGridAdapter(icons, prefs.isShowIconName());
                adapter.setOnIconClickListener(icon -> showIconZoom(icon));
                iconsGrid.setAdapter(adapter);
                if (categoryData != null) adapter.setIconCategoryData(categoryData);
                if (searchInput != null) {
                    String q = searchInput.getText() != null
                            ? searchInput.getText().toString() : "";
                    if (!q.isEmpty()) {
                        int count2 = adapter.filter(q);
                        updateResultsVisibility(count2);
                    } else {
                        updateResultsVisibility(adapter.getFilteredCount());
                    }
                }
            }
        }
    }

    private void updateResultsVisibility(int count) {
        if (noResultsView != null) noResultsView.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        if (iconsGrid != null) iconsGrid.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
    }

    private void showIconZoom(DrawableInfo icon) {
        if (getContext() == null) return;

        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View content = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_icon_preview, null, false);

        ImageView preview = content.findViewById(R.id.icon_preview_image);
        TextView title = content.findViewById(R.id.icon_preview_title);
        TextView category = content.findViewById(R.id.icon_preview_category);
        TextView status = content.findViewById(R.id.icon_preview_status);
        TextView detail = content.findViewById(R.id.icon_preview_category_detail);
        View panel = content.findViewById(R.id.icon_preview_panel);

        if (preview != null) {
            preview.setImageResource(icon.resId);
            preview.setContentDescription(icon.label != null ? icon.label : icon.name);
        }
        if (title != null) {
            title.setText(icon.label != null ? icon.label : icon.name);
        }
        bindCategoryPreview(icon, category, status, detail);
        content.setOnClickListener(v -> dialog.dismiss());
        if (panel != null) {
            panel.setOnClickListener(v -> { });
        }

        dialog.setContentView(content);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.45f;
            window.setAttributes(attributes);
        }
    }

    private void bindCategoryPreview(DrawableInfo icon, TextView categoryView,
                                     TextView statusView, TextView detailView) {
        if (categoryView == null || statusView == null || detailView == null) return;
        IconCategoryEntry entry = categoryData != null && categoryData.iconEntryMap != null
                ? categoryData.iconEntryMap.get(icon.name) : null;
        if (entry == null) {
            categoryView.setVisibility(View.GONE);
            statusView.setVisibility(View.GONE);
            detailView.setVisibility(View.GONE);
            return;
        }

        String categoryText;
        String statusText = "";
        String detailText = "";
        int accent = resolveCategoryColor(entry);

        if ("unmatched".equals(entry.matchStatus)) {
            categoryText = "应用分类：未匹配";
            statusText = "匹配状态：未匹配";
        } else if ("uncategorized".equals(entry.matchStatus)) {
            categoryText = "应用分类：未分类";
            statusText = "匹配状态：已匹配";
        } else if ("multi_category".equals(entry.matchStatus)
                || (entry.categoryIds != null && entry.categoryIds.size() > 1)) {
            categoryText = "应用分类：多分类";
            statusText = "匹配状态：多分类";
            detailText = "具体分类：" + joinCategoryNames(entry);
        } else {
            categoryText = "应用分类：" + joinCategoryNames(entry);
            statusText = "匹配状态：已匹配";
        }

        categoryView.setText(categoryText);
        categoryView.setTextColor(accent);
        statusView.setText(statusText);
        detailView.setText(detailText);
        categoryView.setVisibility(View.VISIBLE);
        statusView.setVisibility(statusText.isEmpty() ? View.GONE : View.VISIBLE);
        detailView.setVisibility(detailText.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private String joinCategoryNames(IconCategoryEntry entry) {
        List<String> names = new ArrayList<>();
        if (entry.categoryIds != null && categoryData != null && categoryData.categoryDefMap != null) {
            for (String id : entry.categoryIds) {
                IconCategoryDef cat = categoryData.categoryDefMap.get(id);
                if (cat != null && cat.name != null && !cat.name.isEmpty()) names.add(cat.name);
            }
        }
        if (names.isEmpty()) return "未分类";
        return android.text.TextUtils.join(" / ", names);
    }

    private int resolveCategoryColor(IconCategoryEntry entry) {
        if (entry.categoryIds != null && !entry.categoryIds.isEmpty()
                && categoryData != null && categoryData.categoryDefMap != null) {
            IconCategoryDef cat = categoryData.categoryDefMap.get(entry.categoryIds.get(0));
            if (cat != null && cat.color != null && !cat.color.isEmpty()) {
                try {
                    return Color.parseColor(cat.color);
                } catch (Exception ignored) {
                }
            }
        }
        return Color.parseColor("#4F7CFF");
    }
}
