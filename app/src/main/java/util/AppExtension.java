package util;

import android.app.Application;

import com.pgyersdk.crash.PgyCrashManager;

import service.ListWidgetService;

public class AppExtension extends Application
{
    private static AppExtension instance;

    public static AppExtension getInstance()
    {
        return instance;
    }

    public static ListWidgetService.ListViewsFactory globalListViewFactory;

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;

        ConfigHelper.ConfigAppSetting();

        ToDoListRef.RestoreData();
        PgyCrashManager.register(this);

//        Thread.setDefaultUncaughtExceptionHandler(
//                new GlobalExceptionHandler()
//              );

    }
}
