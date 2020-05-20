package com.imyyq.rvshowtime;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imyyq.rvshowtime.entity.ItemEntity;
import com.imyyq.rvshowtime.entity.ItemRvEntity;
import com.imyyq.rvshowtime.entity.RvInterface;
import com.imyyq.showtime.RvShowTimeScrollListener;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

public class RvUtil {
    public static void initRv(FragmentManager manager, Fragment fragment, RecyclerView rv) {
        initRv(fragment.getActivity(), rv, manager, fragment);
    }

    public static void initRv(Activity activity, RecyclerView rv) {
        initRv(activity, rv, null, null);
    }

    private static void initRv(Context context, RecyclerView rv, FragmentManager manager, Fragment fragment) {
        List<RvInterface> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            // 增加内部的 rv
            if (i == 15) {
                ItemRvEntity entity = new ItemRvEntity(i, manager != null ? fragment.hashCode() : null);
                for (int j = 0; j < 30; j++) {
                    entity.list.add("inner " + i + ", " + j);
                }
                list.add(entity);
            }
            list.add(new ItemEntity("item " + i));
        }
        rv.setLayoutManager(new LinearLayoutManager(context));

        if (manager != null) {
            rv.addOnScrollListener(new RvShowTimeScrollListener(manager, fragment.hashCode(), list, "example"));
        } else {
            rv.addOnScrollListener(new RvShowTimeScrollListener(list, "example"));
        }

        // 多布局
        MultiItemTypeAdapter<RvInterface> adapter = new MultiItemTypeAdapter<>(context, list);
        adapter.addItemViewDelegate(new TextItemViewDelegate());
        adapter.addItemViewDelegate(new RvItemViewDelegate(context));

        rv.setAdapter(adapter);
    }

    private static class TextItemViewDelegate implements ItemViewDelegate<RvInterface> {
        @Override
        public int getItemViewLayoutId() {
            return R.layout.item_text;
        }

        @Override
        public boolean isForViewType(RvInterface item, int position) {
            return item instanceof ItemEntity;
        }

        @Override
        public void convert(ViewHolder holder, RvInterface rvInterface, int position) {
            TextView textView = holder.getView(R.id.tv);
            textView.setBackgroundColor(position % 2 == 0 ? Color.BLUE : Color.RED);

            ItemEntity entity1 = (ItemEntity) rvInterface;
            textView.setText(entity1.text);
        }
    }

    private static class RvItemViewDelegate implements ItemViewDelegate<RvInterface> {
        private Context context;

        public RvItemViewDelegate(Context context) {
            this.context = context;
        }

        @Override
        public int getItemViewLayoutId() {
            return R.layout.item_rv;
        }

        @Override
        public boolean isForViewType(RvInterface item, int position) {
            return item instanceof ItemRvEntity;
        }

        @Override
        public void convert(ViewHolder holder, RvInterface rvInterface, int position) {
            RecyclerView rv = holder.getView(R.id.rv_inner);

            ItemRvEntity entity = (ItemRvEntity) rvInterface;

            // 添加内部 rv 的监听
            if (entity.getScrollListener() != null) {
                rv.removeOnScrollListener(entity.getScrollListener());
                rv.addOnScrollListener(entity.getScrollListener());
            }

            rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            rv.setAdapter(new CommonAdapter<String>(context, R.layout.item_text_for_inner_rv, entity.list) {
                @Override
                protected void convert(ViewHolder holder, String s, int position) {
                    TextView textView = holder.getView(R.id.tv);
                    textView.setText(s);
                    textView.setBackgroundColor(position % 2 == 0 ? Color.YELLOW : Color.GREEN);
                }
            });
        }
    }
}
