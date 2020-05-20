package com.imyyq.rvshowtime;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imyyq.rvshowtime.databinding.ActivityExampleBinding;
import com.imyyq.showtime.RvShowTimeInterface;
import com.imyyq.showtime.RvShowTimeScrollListener;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ExampleActivity extends AppCompatActivity implements RvShowTimeInterface {
    private ActivityExampleBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<RvInterface> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            // 增加内部的 rv
            if (i == 15) {
                ItemRvEntity entity = new ItemRvEntity(i);
                for (int j = 0; j < 30; j++) {
                    entity.list.add("inner " + i + ", " + j);
                }
                list.add(entity);
            }
            list.add(new ItemEntity("item " + i));
        }
        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.addOnScrollListener(new RvShowTimeScrollListener(list, "example"));

        // 多布局
        MultiItemTypeAdapter<RvInterface> adapter = new MultiItemTypeAdapter<>(this, list);
        adapter.addItemViewDelegate(new TextItemViewDelegate());
        adapter.addItemViewDelegate(new RvItemViewDelegate());

        binding.rv.setAdapter(adapter);
    }

    private class TextItemViewDelegate implements ItemViewDelegate<RvInterface> {
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

    private class RvItemViewDelegate implements ItemViewDelegate<RvInterface> {

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

            rv.setLayoutManager(new LinearLayoutManager(ExampleActivity.this, LinearLayoutManager.HORIZONTAL, false));
            rv.setAdapter(new CommonAdapter<String>(ExampleActivity.this, R.layout.item_text_for_inner_rv, entity.list) {
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
