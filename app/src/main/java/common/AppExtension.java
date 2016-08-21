package common;

import android.app.Application;

import util.AppConfig;
import util.GlobalListLocator;

public class AppExtension extends Application implements Thread.UncaughtExceptionHandler  {
    private static AppExtension instance;

    public static AppExtension getInstance() {
        return instance;
    }

    /**
     * 初始化数据
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        AppConfig.configAppSetting();
        GlobalListLocator.restoreData();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
    }
}
