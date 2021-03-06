package com.dsg.androidperformance;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Debug;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.os.TraceCompat;

import com.dsg.androidperformance.launcherStarter.TaskDispatcher;
import com.dsg.androidperformance.memory.ImageHook;
import com.dsg.androidperformance.tasks.GetDeviceIdTask;
import com.dsg.androidperformance.tasks.InitAMapTask;
import com.dsg.androidperformance.tasks.InitBuglyTask;
import com.dsg.androidperformance.tasks.InitFrescoTask;
import com.dsg.androidperformance.tasks.InitJPushTask;
import com.dsg.androidperformance.tasks.InitStethoTask;
import com.dsg.androidperformance.tasks.InitUmengTask;
import com.dsg.androidperformance.tasks.InitWeexTask;
import com.dsg.androidperformance.utils.LaunchTimer;
import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author DSG
 * @Project AndroidPerformance
 * @date 2020/6/28
 * @describe
 */
public class PerformanceApp extends Application {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_SECONDS = 3;
    private static Application mApplication;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private String mDeviceId;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mApplication = this;
    }

    public static Application getApplication() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LaunchTimer.startRecord();
//        使用traceView
//        Debug.startMethodTracing("App");
        //systrace 查找AppOnCreate 分析
        TraceCompat.beginSection("AppOnCreate");
/*//        这边有几个问题 1.代码不优雅 2.可维护性比较差 3.如果initJPush 依赖Umeng先执行完成  就没办法做到 4.如果需要在生命周期结束之前 initJPush需要执行完成才能执完成 需要一些特殊方法 也不够优雅
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        threadPoolExecutor.execute(initAMap());
        threadPoolExecutor.execute(initBugly());
        threadPoolExecutor.execute(initGetDeviceId());
        threadPoolExecutor.execute(initJPush());
        threadPoolExecutor.execute(initUmeng());
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        //使用启动器 可以解决上面几个问题
        TaskDispatcher.init(PerformanceApp.this);

        TaskDispatcher dispatcher = TaskDispatcher.createInstance();

        dispatcher.addTask(new InitAMapTask())
                .addTask(new InitStethoTask())
                .addTask(new InitWeexTask())
                .addTask(new InitBuglyTask())
                .addTask(new InitFrescoTask())
                .addTask(new InitJPushTask())
                .addTask(new InitUmengTask())
                .addTask(new GetDeviceIdTask())
                .start();

        dispatcher.await();
//        Debug.stopMethodTracing();
        TraceCompat.endSection();
        LaunchTimer.endRecord("app start");

        DexposedBridge.hookAllConstructors(ImageView.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                DexposedBridge.findAndHookMethod(ImageView.class, "setImageBitmap", Bitmap.class, new ImageHook());
            }
        });
    }


    private Runnable initGetDeviceId() {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    private Runnable initJPush() {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    private Runnable initUmeng() {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    private Runnable initBugly() {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    private Runnable initAMap() {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void setDeviceId(String mDeviceId) {
        this.mDeviceId = mDeviceId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }
}
