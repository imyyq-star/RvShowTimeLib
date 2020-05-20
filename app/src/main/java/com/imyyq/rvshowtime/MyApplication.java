package com.imyyq.rvshowtime;

import android.app.Application;

import com.imyyq.showtime.RvShowTimeManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RvShowTimeManager.setApplication(this);
    }
}
