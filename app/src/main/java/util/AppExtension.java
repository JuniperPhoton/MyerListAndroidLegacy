package util;

import android.app.Application;
import android.content.Intent;

import com.pgyersdk.crash.PgyCrashManager;

import activity.StartActivity;

public class AppExtension extends Application {
    private static AppExtension instance;

    public static AppExtension getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;

        ConfigHelper.configAppSetting();

        ToDoListGlobalLocator.restoreData();
        PgyCrashManager.register(this);

        String access_token = ConfigHelper.getString(this, "access_token");
        boolean offline = ConfigHelper.getBoolean(this, "offline_mode");

        ConfigHelper.ISOFFLINEMODE = offline;

        //还没有登录/进入离线模式，回到 StartActivity
        if (!offline && access_token == null) {
            ConfigHelper.ISOFFLINEMODE = false;
            Intent intent = new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
