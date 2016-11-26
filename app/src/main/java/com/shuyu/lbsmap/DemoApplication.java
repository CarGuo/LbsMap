package com.shuyu.lbsmap;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

import java.util.concurrent.atomic.AtomicReference;

public class DemoApplication extends Application {

    private static AtomicReference<DemoApplication> mInstance = new AtomicReference<DemoApplication>();

    public static String SK() {
        return "";
    }

    public static String AK() {
        return "r3sHA6uyjCwDvE838WGfvnPSpghTxi93";
    }

    public static int TABLE_ID() {
        return 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance.set(this);
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
    }

    public static DemoApplication get() {
        return mInstance.get();
    }

}