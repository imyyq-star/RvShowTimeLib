package com.imyyq.rvshowtime.entity;

import com.imyyq.showtime.RvShowTimeInnerInterface;
import com.imyyq.showtime.RvShowTimeScrollListener;

import java.util.ArrayList;
import java.util.List;

public class ItemRvEntity implements RvInterface, RvShowTimeInnerInterface {
    public List<String> list = new ArrayList<>();

    private RvShowTimeScrollListener listener;

    public ItemRvEntity(int inOuterPosition, Integer fragmentHashCode) {
        if (fragmentHashCode != null) {
            listener = new RvShowTimeScrollListener(fragmentHashCode, list, inOuterPosition, "example inner");
        } else {
            listener = new RvShowTimeScrollListener(list, inOuterPosition, "example inner");
        }
    }

    @Override
    public RvShowTimeScrollListener getScrollListener() {
        return listener;
    }
}
