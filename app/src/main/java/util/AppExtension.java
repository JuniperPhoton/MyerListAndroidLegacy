package util;

import android.app.Application;
import android.content.Intent;

import com.pgyersdk.crash.PgyCrashManager;

import activity.MainActivity;
import activity.StartActivity;

public class AppExtension extends Application implements Thread.UncaughtExceptionHandler  {
    private static AppExtension instance;

    public static AppExtension getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        ConfigHelper.configAppSetting();

        GlobalListLocator.restoreData();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
    }
}
