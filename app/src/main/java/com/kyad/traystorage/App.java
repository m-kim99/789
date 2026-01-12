package com.kyad.traystorage;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.databinding.BindingAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.kyad.traystorage.data.DataManager;

public class App extends Application {
    private final List<Activity> activities = new ArrayList<>();

    public enum AppStatus {
        BACKGROUND,               // app is background
        RETURNED_TO_FOREGROUND,   // app returned to foreground(or first launch)
        FOREGROUND,               // app is foreground
        EXITED,                   // app is foreground
    }

    private final AppStatus appStatus = AppStatus.FOREGROUND;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private static App instance;

    public static App get() {
        if (instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

        instance = this;
        DataManager.inject(this);

        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerApplication());

        //ChannelIO.initialize(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    private void addActivity(Activity act) {
        activities.add(act);
    }

    private void removeActivity(Activity act) {
        activities.remove(act);
    }

    public void finishAllActivity() {
        if (activities != null) {
            synchronized (activities) {
                for (Activity act : activities) {
                    act.finish();
                }
            }
        }
    }

    public void finishAllActivityByTarget(Class target) {
        if (activities != null) {
            synchronized (activities) {
                for (int i = activities.size() - 1; i >= 0; i--) {
                    Activity act = activities.get(i);
                    Class cls = act.getClass();
                    if (cls.equals(target)) {
                        break;
                    }
                    act.finish();
                }
            }
        }
    }

    public void exitApp() {
        if (activities != null) {
            synchronized (activities) {
                for (Activity act : activities) {
                    act.finish();
                }
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public static boolean isBackground() {
        ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);
        boolean isInBackground = myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
        return isInBackground;
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private final int running = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            addActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            removeActivity(activity);
        }
    }

    class UncaughtExceptionHandlerApplication implements Thread.UncaughtExceptionHandler {
        private String getStackTrace(Throwable th) {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);

            Throwable cause = th;
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }

            final String stacktraceAsString = result.toString();
            printWriter.close();

            return stacktraceAsString;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("uncaughtException", getStackTrace(ex));
            uncaughtExceptionHandler.uncaughtException(thread, ex);
        }
    }
}
