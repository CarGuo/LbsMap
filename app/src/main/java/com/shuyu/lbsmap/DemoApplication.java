package com.shuyu.lbsmap;

import android.app.Application;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import java.util.concurrent.atomic.AtomicReference;

import cn.finalteam.okhttpfinal.OkHttpFinal;
import cn.finalteam.okhttpfinal.OkHttpFinalConfiguration;
import de.greenrobot.event.EventBus;

public class DemoApplication extends Application {

    private static AtomicReference<DemoApplication> mInstance = new AtomicReference<>();

    public final static int PAGE_SIZE = 20;//最大可以50

    private EventBus mEventBus;

    private JobManager mJobManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance.set(this);

        mEventBus = EventBus.getDefault();

        Configuration netConfig = new Configuration.Builder(this).minConsumerCount(1)
                .maxConsumerCount(5)
                .loadFactor(2)
                .consumerKeepAlive(120)
                .id("JobManager").customLogger(new CustomLogger() {
                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String s, Object... objects) {
                        Log.d("JOB", String.format("Debug:%s", s));
                    }

                    @Override
                    public void e(Throwable throwable, String s, Object... objects) {
                        Log.d("JOB", String.format("Error:%s", s));
                    }

                    @Override
                    public void e(String s, Object... objects) {
                        Log.d("JOB", String.format("Error:%s", s));
                    }
                }).build();
        mJobManager = new JobManager(this, netConfig);

        OkHttpFinalConfiguration.Builder builder = new OkHttpFinalConfiguration.Builder();
        OkHttpFinal.getInstance().init(builder.build());

        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
    }

    public static DemoApplication getApplication() {
        return mInstance.get();
    }

    public EventBus getEventBus() {
        return mEventBus;
    }

    public JobManager getJobManager() {
        return mJobManager;
    }

    public static String SK() {
        return "PWTIyZGdbBTtXri84Oj3NC932DhxXN8n";
    }

    public static String AK() {
        return "r3sHA6uyjCwDvE838WGfvnPSpghTxi93";
    }

    public static int TABLE_ID() {
        return 158714;
    }

}