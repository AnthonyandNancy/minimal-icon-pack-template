package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.ui.BackBarHelper;
import com.template.iconpack.ui.adapters.FaqAdapter;

import java.util.Arrays;
import java.util.List;

public class FaqFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        RecyclerView faqList = view.findViewById(R.id.faq_list);
        faqList.setLayoutManager(new LinearLayoutManager(getContext()));

        List<FaqAdapter.FaqItem> items = Arrays.asList(
                new FaqAdapter.FaqItem(
                        "如何应用图标包？",
                        "打开你的启动器设置，找到\"" +
                        "主题\"或\"图标包\"选项，选择本图标包即可应用。不同启动器的设置位置可能有所不同。"),
                new FaqAdapter.FaqItem(
                        "为什么有些图标没有变化？",
                        "图标包只会替换已适配的应用图标。如果某个应用的图标没有变化，" +
                        "说明该应用尚未适配。你可以在\"申请图标\"页面提交申请。"),
                new FaqAdapter.FaqItem(
                        "如何申请缺失图标？",
                        "在\"申请图标\"页面，你可以看到所有已安装应用中未适配的列表。" +
                        "勾选需要申请的应用，点击\"导出\"或\"分享\"按钮即可将申请列表发送给开发者。"),
                new FaqAdapter.FaqItem(
                        "为什么某些启动器不支持？",
                        "图标包使用 Android 标准图标主题接口。大部分主流启动器（Nova、" +
                        "Lawnchair、Smart Launcher 等）都支持。如果你的启动器不支持，请尝试切换到兼容的启动器。")
        );

        FaqAdapter adapter = new FaqAdapter();
        adapter.setItems(items);
        faqList.setAdapter(adapter);

        BackBarHelper.setup(view, getActivity());
        return view;
    }
}
