package com.imyyq.rvshowtime;

import com.imyyq.showtime.RvShowTimeInnerInterface;
import com.imyyq.showtime.RvShowTimeScrollListener;

import java.util.ArrayList;
import java.util.List;

public class ItemRvEntity implements RvInterface, RvShowTimeInnerInterface {
    public List<String> list = new ArrayList<>();

    private RvShowTimeScrollListener listener;

    public ItemRvEntity(int inOuterPosition) {
        listener = new RvShowTimeScrollListener(list, inOuterPosition, "example inner");
    }

    @Override
    public RvShowTimeScrollListener getScrollListener() {
        return listener;
    }
}
