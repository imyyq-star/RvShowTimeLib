package com.zhy.adapter.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ItemViewDelegateManager;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.baseadapter_recyclerview.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by zhy on 16/4/9.
 */
public class MultiItemTypeAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    protected Context mContext;
    protected List<T> mDatas;

    protected ItemViewDelegateManager mItemViewDelegateManager;
    protected OnItemClickListener mOnItemClickListener;

    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public MultiItemTypeAdapter(Context context, List<T> datas) {
        mContext = context;
        mDatas = datas;
        mItemViewDelegateManager = new ItemViewDelegateManager();

        mOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnItemClickListener != null) {
                    int position = (int) v.getTag(R.id.tag_for_position);
                    RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag(
                            R.id.tag_for_view_holder);
                    mOnItemClickListener.onItemClick(v, viewHolder, position);
                }
            }
        };

        mOnLongClickListener = new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (mOnItemClickListener != null) {
                    int position = (int) v.getTag(R.id.tag_for_position);
                    RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag(
                            R.id.tag_for_view_holder);
                    return mOnItemClickListener.onItemLongClick(v, viewHolder, position);
                }
                return false;
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        if (!useItemViewDelegateManager()) return super.getItemViewType(position);
        return mItemViewDelegateManager.getItemViewType(mDatas.get(position), position);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull
                    ViewGroup parent, int viewType) {
        ItemViewDelegate itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemViewDelegate.getItemViewLayoutId();
        ViewHolder holder = ViewHolder.createViewHolder(mContext, parent, layoutId);
        onViewHolderCreated(holder,holder.getConvertView());
        holder.getConvertView().setTag(R.id.tag_for_view_type, viewType);
        return holder;
    }

    public void onViewHolderCreated(ViewHolder holder,View itemView){

    }

    public void convert(ViewHolder holder, T t) {
        mItemViewDelegateManager.convert(holder, t, holder.getAdapterPosition());
    }

    protected boolean isEnabled(int viewType) {
        return true;
    }

    @Override
    public void onBindViewHolder(
            @NonNull
                    ViewHolder holder, int position) {
        View convertView = holder.getConvertView();
        int viewType = (int) convertView.getTag(R.id.tag_for_view_type);
        if (isEnabled(viewType))
        {
            convertView.setTag(R.id.tag_for_position, position);
            convertView.setTag(R.id.tag_for_view_holder, holder);
            convertView.setOnClickListener(mOnClickListener);
            convertView.setOnLongClickListener(mOnLongClickListener);
        }
        else
        {
            convertView.setOnClickListener(null);
            convertView.setOnLongClickListener(null);
        }

        convert(holder, mDatas.get(position));
    }

    @Override
    public int getItemCount() {
        int itemCount = mDatas.size();
        return itemCount;
    }


    public List<T> getDatas() {
        return mDatas;
    }

    public MultiItemTypeAdapter addItemViewDelegate(ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(itemViewDelegate);
        return this;
    }

    public MultiItemTypeAdapter addItemViewDelegate(int viewType, ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(viewType, itemViewDelegate);
        return this;
    }

    protected boolean useItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder holder,  int position);

        boolean onItemLongClick(View view, RecyclerView.ViewHolder holder,  int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}
