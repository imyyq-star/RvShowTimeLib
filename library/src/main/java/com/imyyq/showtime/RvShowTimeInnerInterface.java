package com.imyyq.showtime;

/**
 * 内部 rv 对应的数据项必须实现此接口
 */
public interface RvShowTimeInnerInterface {
    /**
     * 内部列表对应的 scrollListener
     */
    RvShowTimeScrollListener getScrollListener();
}
