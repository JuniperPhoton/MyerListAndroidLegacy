package helper;

import android.app.Application;

import com.pgyersdk.crash.PgyCrashManager;

import model.ToDoListHelper;

public class ContextUtil extends Application
{
    private static ContextUtil instance;

    public static ContextUtil getInstance() {
        return instance;
    }

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;

        ConfigHelper.ConfigAppSetting();

        ToDoListHelper.SetUpSavedData();
        PgyCrashManager.register(this);

    }
}
