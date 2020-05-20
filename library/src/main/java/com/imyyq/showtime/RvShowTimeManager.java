package com.imyyq.showtime;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseLongArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rv 数据曝光时间管理类。用来计算每一个 item 的曝光时间。
 * 监听对应界面的生命周期，当界面不可见时上报曝光数据。在界面销毁时停止监听。
 * <p>
 * 每个列表 item 只有显示在屏幕上了，才会开始计时，通过滑动消失在屏幕上了，才会结束计时。
 * <p>
 * 给 Rv 加曝光功能分为以下步骤：
 * 0. 初始化 {@link RvShowTimeManager setApplication}
 * 1、需要曝光的 rv 增加 rv.addOnScrollListener 添加 {@link RvShowTimeScrollListener}
 * 2、列表所属界面需实现 {@link RvShowTimeInterface}，复写接口方法接收曝光数据
 * 3、如果有内嵌的横滑列表，该列表属于内部列表，其所在数据列表需要实现 {@link RvShowTimeInnerInterface}
 */
@SuppressWarnings("WeakerAccess")
public class RvShowTimeManager {
    private String debugLabel; // 测试时用的

    // 界面生命周期回到，只有一个回调实例
    private static FragmentCallback mFragmentCallback;
    private ActivityCallback mActivityCallback;

    // 每一个 fragment 都对应一个 manager，用来计时的
    private static ArrayMap<Integer, RvShowTimeManager> mFragmentRvManagerMap = new ArrayMap<>();
    private static ArrayMap<Integer, List<RvShowTimeManager>> mFragmentInnerRvManagerMap = new ArrayMap<>();

    // Key 是 fragmentManager 的 hashCode，布尔值表示此 FragmentManager 下的 Fragment 是否已注册监听
    private static ArrayMap<Integer, Boolean> mFragmentManagerMap = new ArrayMap<>();
    // Key 是 fragmentManager 的 hashCode，Set 表示这个 fm 下的所有 fragment 的 hashCode，用来解除监听的
    private static ArrayMap<Integer, Set<Integer>> mFragmentMap = new ArrayMap<>();

    // 存储旧的位置信息
    private int mOldFirstPos = -1;
    private int mOldLastPos = -1;

    // 曝光时间
    private SparseLongArray mShowDurationArr = new SparseLongArray();
    // 开始曝光时的时间，用来辅助计算曝光时间的
    private SparseLongArray mStartShowTimeArr = new SparseLongArray();

    private List mDataList;
    private boolean mIsInnerRv = false; // 外部rv才需要遍历内部rv，true：当前是内部rv
    private Integer mInnerRvInOuterRvPos = null; // 内部rv在外部rv中的position
    private boolean mIsOuterRvResume = false; // 内部rv在外部rv中是否可见

    private static Application mApplication;

    /**
     * 初始化
     *
     * @param mApplication 当前应用的 application
     */
    public static void setApplication(Application mApplication) {
        RvShowTimeManager.mApplication = mApplication;
    }

    /**
     * 给 activity rv 使用
     *
     * @param data rv 的数据
     */
    public RvShowTimeManager(List data, String debugLabel) {
        this(data, false, null, debugLabel);
    }

    /**
     * 给 Activity 的内部rv用的
     *
     * @param data      内部rv数据
     * @param isInnerRv 应该都为 true，因为不能和上面的构造方法的签名冲突
     */
    public RvShowTimeManager(List data, boolean isInnerRv, Integer innerRvPosition, String debugLabel) {
        this.debugLabel = debugLabel;
        this.mDataList = data;
        this.mIsInnerRv = isInnerRv;
        this.mInnerRvInOuterRvPos = innerRvPosition;

        mActivityCallback = new ActivityCallback();

        mApplication.registerActivityLifecycleCallbacks(mActivityCallback);
    }

    /**
     * 给 Fragment Rv 内部 Rv 使用的
     *
     * @param fragmentHashCode Fragment 的 code
     * @param data             内部rv的数据
     */
    public RvShowTimeManager(int fragmentHashCode, List data, Integer innerRvPosition, String debugLabel) {
        this.debugLabel = debugLabel;
        this.mDataList = data;
        this.mIsInnerRv = true;
        this.mInnerRvInOuterRvPos = innerRvPosition;

        // 可能有多个内部 rv
        List<RvShowTimeManager> list = mFragmentInnerRvManagerMap.get(fragmentHashCode);
        if (list == null) {
            list = new ArrayList<>();
            mFragmentInnerRvManagerMap.put(fragmentHashCode, list);
        }
        list.add(this);
    }

    /**
     * fragment rv 使用的
     *
     * @param fragmentManager  该 fragment 所属的 fragmentManager
     * @param fragmentHashCode fragment 的 HashCode
     * @param data             数据
     */
    public RvShowTimeManager(FragmentManager fragmentManager, int fragmentHashCode, List data, String debugLabel) {
        this.debugLabel = debugLabel;
        this.mDataList = data;

        // 缓存 fragment 的信息，用来解除监听，使用 set 防止重复
        Set<Integer> fragmentCodes = mFragmentMap.get(fragmentManager.hashCode());
        if (fragmentCodes == null) {
            fragmentCodes = new HashSet<>();
            mFragmentMap.put(fragmentManager.hashCode(), fragmentCodes);
        }
        fragmentCodes.add(fragmentHashCode);

        mFragmentRvManagerMap.put(fragmentHashCode, this);

        // 确保一个 fm 只注册一次
        Boolean isRegister = mFragmentManagerMap.get(fragmentManager.hashCode());
        log("RvShowTimeManager", "RvShowTimeManager: " + fragmentManager + ", " + "， isRegister=" + isRegister, debugLabel, false);
        if (isRegister == null || !isRegister) {
            // 注册 fragment 声明周期
            mFragmentCallback = new FragmentCallback();
            fragmentManager.registerFragmentLifecycleCallbacks(mFragmentCallback, true);
            mFragmentManagerMap.put(fragmentManager.hashCode(), true);
        }
    }

    /**
     * 设置当前新的位置，根据当前新的位置来设置旧位置的曝光时间
     *
     * @param newFirstPos 当前屏幕上第一个显示的位置
     * @param newLastPos  当前屏幕上最后一个显示的位置
     */
    public void setPosition(int newFirstPos, int newLastPos) {
        // 没变化则不动
        if (newFirstPos == mOldFirstPos && newLastPos == mOldLastPos) return;

        // 初始时间
        if (mOldFirstPos == -1 && mOldLastPos == -1) {
            resetTime(newFirstPos, newLastPos);
        } else {
            // 确定是否有交集，交集的情况通常是内嵌的 rv 中出现
            if (newFirstPos > mOldLastPos || mOldFirstPos > newLastPos) {
                calcTime(mOldFirstPos, mOldLastPos);
                resetTime(newFirstPos, newLastPos);
            } else {
                // 往右或往下移动了，计算前面隐藏掉的item曝光时间
                if (newFirstPos > mOldFirstPos) {
                    calcTime(mOldFirstPos, newFirstPos - 1);
                }
                // 否则往左或往上移动了，重置前面新显示出来的item的时间
                else if (newFirstPos < mOldFirstPos) {
                    resetTime(newFirstPos, mOldFirstPos - 1);
                }

                // 往右或往下移动了，重置后面新显示出来的item的时间
                if (newLastPos > mOldLastPos) {
                    resetTime(mOldLastPos + 1, newLastPos);
                }
                // 否则往左或往上移动了，计算后面隐藏掉的item曝光时间
                else if (newLastPos < mOldLastPos) {
                    calcTime(newLastPos + 1, mOldLastPos);
                }
            }
        }
        mOldFirstPos = newFirstPos;
        mOldLastPos = newLastPos;

        if (BuildConfig.DEBUG)
            log("RViewShowTimeManager", "setPosition: " + "\t" + newFirstPos + "\t" + newLastPos, debugLabel, false);
        print();
    }

    private void print() {
        if (BuildConfig.DEBUG) {
            int pos = 0;
            if (mShowDurationArr.get(pos, -1) == -1) return;
            log("RViewShowTimeManager", "time: " + "\t" + mShowDurationArr.keyAt(pos) + "\t" + mShowDurationArr.valueAt(pos), debugLabel, false);
        }
    }

    /**
     * 计算隐藏掉的 item 的曝光时间
     *
     * @param startPosition 包含头
     * @param endPosition   包含尾
     */
    private void calcTime(int startPosition, int endPosition) {
        if (startPosition == -1 || endPosition == -1) return;

        log("RvShowTimeManager", "calcTime " + "\t" + startPosition + "\t" + endPosition, debugLabel, false);
        for (int i = startPosition; i <= endPosition; i++) {
            // 当前时间减去他们开始曝光的时间，就是曝光时间
            long value = mStartShowTimeArr.get(i, -1);
            if (value == -1) { // 虽然不太可能找不到，但这里也做下防护
                if (BuildConfig.DEBUG) {
                    throw new RuntimeException("startShowTime 为-1，position=" + i);
                }
            } else {
                // 和之前的曝光时间加在一起
                long oldTime = mShowDurationArr.get(i, -1);
                if (oldTime != -1) {
                    mShowDurationArr.put(i, System.currentTimeMillis() - value + oldTime);
                } else {
                    mShowDurationArr.put(i, System.currentTimeMillis() - value);
                }
            }
        }
    }

    /**
     * 新显示出来的 item 重新计时
     *
     * @param startPosition 包含头
     * @param endPosition   包含尾
     */
    private void resetTime(int startPosition, int endPosition) {
        if (startPosition == -1 || endPosition == -1) return;
        for (int i = startPosition; i <= endPosition; i++) {
            mStartShowTimeArr.put(i, System.currentTimeMillis());
        }
    }

    private void report(RvShowTimeInterface reportInterface) {
        if (mDataList == null || mDataList.isEmpty()) {
            return;
        }
        // 拿到所有的数据，并拿到需要上报的信息
        ArrayMap<Integer, Pair<Long, Object>> map = new ArrayMap<>();
        for (int j = 0; j < mShowDurationArr.size(); j++) {
            int position = mShowDurationArr.keyAt(j);
            if (position < 0 || position >= mDataList.size()) {
                log("ActivityCallback", "onActivityDestroyed: 索引越界 = " + position, debugLabel, false);
                continue;
            }

            long time = mShowDurationArr.valueAt(j);
            Object obj = mDataList.get(position);
            map.put(position, new Pair<>(time, obj));
        }
        if (reportInterface != null) {
            if (mIsInnerRv && mInnerRvInOuterRvPos != null) {
                reportInterface.onInnerReport(mInnerRvInOuterRvPos, map);
            } else {
                reportInterface.onOuterReport(map);
            }
        }
        mShowDurationArr.clear();
    }

    public void resume() {
        resume(false);
    }

    /**
     * @param isOuterResume 是否是外部的界面，比如activity或fragment resume了
     */
    private void resume(boolean isOuterResume) {
        if (mIsInnerRv) {
            if (isOuterResume) { // 外部界面的resume事件，只有内部列表resume了才需要继续往下执行
                if (!mIsOuterRvResume) {
                    return;
                }
            } else {
                if (mIsOuterRvResume) { // 内部列表resume事件，如果已经展示，则不重复展示
                    return;
                }
            }
        }

        log("RvShowTimeManager", "resume: reset", debugLabel, false);
        resetTime(mOldFirstPos, mOldLastPos);
        if (!isOuterResume) {
            mIsOuterRvResume = true;
        }
    }

    public void pause() {
        pause(false);
    }

    private void pause(boolean isOuterPause) {
        if (mIsInnerRv) {
            if (isOuterPause) { // 外部界面的pause事件，只有内部列表resume了才需要继续往下执行
                if (!mIsOuterRvResume) {
                    return;
                }
            } else {
                if (!mIsOuterRvResume) { // 内部列表pause事件，如果已经pause，则不重复展示
                    return;
                }
            }
        }

        log("RvShowTimeManager", "pause: calc", debugLabel, false);
        if (!isOuterPause) {
            mIsOuterRvResume = false;
        }
        calcTime(mOldFirstPos, mOldLastPos);
        if (BuildConfig.DEBUG) {
            log("ActivityCallback", "onActivityPaused: " + mStartShowTimeArr.size() + "\t" + mShowDurationArr.size() + "\t" + mDataList.size(), debugLabel, true);
            if (!mIsInnerRv) {
                for (int i = 0; i < mStartShowTimeArr.size(); i++) {
                    if (mShowDurationArr.get(mStartShowTimeArr.keyAt(i), -100) == -100) {
                        log("ActivityCallback", "onActivityPaused: 遗漏： " + mStartShowTimeArr.keyAt(i), debugLabel, true);
                    }
                }
            }
        }
        print();
    }

    private class ActivityCallback implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            // 页面重新可见了，重新计算时间
            if (activity instanceof RvShowTimeInterface) {
                resume(true);
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            // activity 进入后台了，相当于当前所有可见的都隐藏了
            if (activity instanceof RvShowTimeInterface) {
                pause(true);
                report((RvShowTimeInterface) activity);
            }
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            log("ActivityCallback", "onActivityDestroyed", debugLabel, false);
            mApplication.unregisterActivityLifecycleCallbacks(mActivityCallback);
        }
    }

    private static class FragmentCallback extends FragmentManager.FragmentLifecycleCallbacks {
        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log("FragmentCallback", "onFragmentResumed: " + fm + ", ", "", false);

            // 可见时才计时
            RvShowTimeManager manager = mFragmentRvManagerMap.get(f.hashCode());
            if (manager != null) {
                manager.resume(true);
            }
            List<RvShowTimeManager> list = mFragmentInnerRvManagerMap.get(f.hashCode());
            if (list != null && !list.isEmpty()) {
                for (RvShowTimeManager showTimeManager : list) {
                    showTimeManager.resume(true);
                }
            }
        }

        @Override
        public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
            if (!(f instanceof RvShowTimeInterface)) return;

            // 不可见时上报
            RvShowTimeManager manager = mFragmentRvManagerMap.get(f.hashCode());
            if (manager != null) {
                log("FragmentCallback", "onFragmentPaused: " + fm + ", ", manager.debugLabel, false);
                manager.pause(true);
                manager.report((RvShowTimeInterface) f);
            }
            List<RvShowTimeManager> list = mFragmentInnerRvManagerMap.get(f.hashCode());
            if (list != null && !list.isEmpty()) {
                for (RvShowTimeManager showTimeManager : list) {
                    showTimeManager.pause(true);
                    showTimeManager.report((RvShowTimeInterface) f);
                }
            }
        }

        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log("FragmentCallback", "onFragmentDestroyed: " + fm + ", ", "", false);
            // 销毁了，则执行清理
            Set<Integer> list = mFragmentMap.get(fm.hashCode());
            if (list != null) {
                list.remove(f.hashCode());
            }

            mFragmentRvManagerMap.remove(f.hashCode());
            mFragmentInnerRvManagerMap.remove(f.hashCode());

            // 取消注册，只有所有的 fragment 都回调了 Destroyed 才取消
            if (fm.isDestroyed() && mFragmentCallback != null && mFragmentMap.get(fm.hashCode()) != null) {
                log("FragmentCallback", "onFragmentDestroyed: unregister", "", false);
                mFragmentManagerMap.remove(fm.hashCode());
                mFragmentMap.remove(fm.hashCode());
                fm.unregisterFragmentLifecycleCallbacks(mFragmentCallback);
            }
        }
    }

    private static void log(String tag, String msg, String label, boolean isE) {
        if (!BuildConfig.DEBUG) return;
        msg = label + " commonLog - " + msg;
        if (isE) {
            Log.e(tag, msg);
        } else {
            Log.i(tag, msg);
        }
    }
}
