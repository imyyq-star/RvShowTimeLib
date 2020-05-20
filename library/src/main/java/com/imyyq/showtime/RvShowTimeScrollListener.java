package com.imyyq.showtime;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RvShowTimeScrollListener extends RecyclerView.OnScrollListener {
    private String debugLabel; // 测试时用的

    private RvShowTimeManager mOuterShowTimeManager;
    private List mOuterData;

    private boolean mIsInnerRv = false; // 外部rv才需要遍历内部rv
    private Integer mInnerRvInOuterRvPos = null; // 内部rv在外部rv中的position

    public RvShowTimeScrollListener(List data, String debugLabel) {
        mOuterData = data;
        this.mOuterShowTimeManager = new RvShowTimeManager(data, debugLabel);
    }

    /**
     * 给activity的Rv的内部Rv用的
     *
     * @param data            内部Rv数据
     * @param innerRvPosition 内部Rv在外部Rv中的位置
     */
    public RvShowTimeScrollListener(List data, Integer innerRvPosition, String debugLabel) {
        this.mOuterShowTimeManager = new RvShowTimeManager(data, true, innerRvPosition, debugLabel);
        this.mIsInnerRv = true;
        this.mInnerRvInOuterRvPos = innerRvPosition;
    }

    /**
     * Fragment 使用
     *
     * @param fragmentManager  FragmentManager
     * @param fragmentHashCode FragmentCode
     * @param data             数据
     * @param debugLabel       测试 Label
     */
    public RvShowTimeScrollListener(FragmentManager fragmentManager, int fragmentHashCode, List data, String debugLabel) {
        mOuterData = data;
        this.mOuterShowTimeManager = new RvShowTimeManager(fragmentManager, fragmentHashCode, data, debugLabel);
    }

    /**
     * 给Fragment的Rv内部Rv使用的
     *
     * @param fragmentHashCode Fragment 的 code
     * @param data             内部Rv数据
     * @param innerRvPosition  内部列表在外部Rv中的位置
     */
    public RvShowTimeScrollListener(int fragmentHashCode, List data, Integer innerRvPosition, String debugLabel) {
        this.mOuterShowTimeManager = new RvShowTimeManager(fragmentHashCode, data, innerRvPosition, debugLabel);

        this.mIsInnerRv = true;
        this.mInnerRvInOuterRvPos = innerRvPosition;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) return;

        // 找到当前屏幕中显示的子项位置信息
        Integer firstVisibleItemPosition = null;
        Integer lastVisibleItemPosition = null;

        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager manager = (GridLayoutManager) layoutManager;

            firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
            lastVisibleItemPosition = manager.findLastVisibleItemPosition();

        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager manager = (LinearLayoutManager) layoutManager;

            firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
            lastVisibleItemPosition = manager.findLastVisibleItemPosition();
        }

        if (firstVisibleItemPosition == null) return;

        if (mOuterShowTimeManager != null) {
            mOuterShowTimeManager.setPosition(firstVisibleItemPosition, lastVisibleItemPosition);
        }

        // 外部的 rv 的滚动，会影响内部 rv 的显示和隐藏

        // 当前是外部 rv，那么找到内部的 rv 进行操作
        if (!mIsInnerRv) {
            for (Object data : mOuterData) {
                // 内部的 rv 必须实现此接口，并提供其 scrollListener
                if (data instanceof RvShowTimeInnerInterface) {
                    RvShowTimeInnerInterface innerInterface = (RvShowTimeInnerInterface) data;
                    RvShowTimeScrollListener listener = innerInterface.getScrollListener();
                    if (listener == null) continue;

                    // 当前的 scrollListener 必须是内部的，且必须有位置
                    if (listener.mIsInnerRv && listener.mInnerRvInOuterRvPos != null) {
                        // 根据内部 rv 是否出现在屏幕中来对其进行 resume 和 pause
                        if (listener.mInnerRvInOuterRvPos >= firstVisibleItemPosition && listener.mInnerRvInOuterRvPos <= lastVisibleItemPosition) {
                            listener.mOuterShowTimeManager.resume();
                        } else {
                            listener.mOuterShowTimeManager.pause();
                        }
                    }
                }
            }
        }
    }
}
