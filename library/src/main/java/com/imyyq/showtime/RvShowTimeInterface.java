package com.imyyq.showtime;

import android.util.Log;

import androidx.collection.ArrayMap;
import androidx.core.util.Pair;

import java.util.Map;

/**
 * rv 所在的界面必须实现此接口
 */
public interface RvShowTimeInterface {
    default void onOuterReport(ArrayMap<Integer, Pair<Long, Object>> map) {
        for (Map.Entry<Integer, Pair<Long, Object>> entry : map.entrySet()) {
            Log.i("RvShowTimeInterface", "commonLog - onOuterReport: " + entry.getKey() + "\t" + entry.getValue());
        }
        Log.i("RvShowTimeInterface", "commonLog - onOuterReport: ==============================================");
    }

    default void onInnerReport(int inOuterPosition, ArrayMap<Integer, Pair<Long, Object>> map) {
        for (Map.Entry<Integer, Pair<Long, Object>> entry : map.entrySet()) {
            Log.i("RvShowTimeInterface", "commonLog - onInnerReport: " + inOuterPosition + "\t" + entry.getKey() + "\t" + entry.getValue());
        }
        Log.i("RvShowTimeInterface", "commonLog - onInnerReport: "+inOuterPosition+"================================================");
    }
}
