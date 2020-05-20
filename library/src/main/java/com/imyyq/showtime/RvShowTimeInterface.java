package com.imyyq.showtime;

import android.util.Log;

import androidx.collection.ArrayMap;
import androidx.core.util.Pair;

import java.util.Map;

/**
 * rv 所在的界面必须实现此接口
 */
public interface RvShowTimeInterface {
    /**
     * 外部的 rv 曝光回调
     *
     * @param map 已经曝光过的数据 map，key 是 position，map 是曝光时间和对应的数据
     */
    default void onOuterReport(ArrayMap<Integer, Pair<Long, Object>> map) {
        for (Map.Entry<Integer, Pair<Long, Object>> entry : map.entrySet()) {
            Log.i("RvShowTimeInterface", "commonLog - onOuterReport: " + entry.getKey() + "\t" + entry.getValue());
        }
        Log.i("RvShowTimeInterface", "commonLog - onOuterReport: ==============================================");
    }

    /**
     * 内部的 rv 曝光回调
     *
     * @param inOuterPosition 内部 rv 在外部 rv 中的 position
     * @param map             已经曝光过的数据 map，key 是 position，map 是曝光时间和对应的数据
     */
    default void onInnerReport(int inOuterPosition, ArrayMap<Integer, Pair<Long, Object>> map) {
        for (Map.Entry<Integer, Pair<Long, Object>> entry : map.entrySet()) {
            Log.i("RvShowTimeInterface", "commonLog - onInnerReport: " + inOuterPosition + "\t" + entry.getKey() + "\t" + entry.getValue());
        }
        Log.i("RvShowTimeInterface", "commonLog - onInnerReport: " + inOuterPosition + "================================================");
    }
}
