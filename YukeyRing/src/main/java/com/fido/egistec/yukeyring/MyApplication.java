package com.fido.egistec.yukeyring;

import android.app.Application;

/**
 * Created by Administrator on 2016/8/22.
 */
public class MyApplication extends Application {
    private static MyApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static MyApplication getApplication() {
        return sApplication;
    }
}
