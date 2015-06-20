package helper;

import android.app.Application;
import android.content.Intent;

import activity.MainActivity;
import activity.StartActivity;
import model.ScheduleList;

public class ContextUtil extends Application {
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

        ScheduleList.SetUpSavedData();

//        String email=ConfigHelper.getString(this,"email");
//        boolean offline=ConfigHelper.getBoolean(this,"offline_mode");
//        if(offline || email!=null )
//        {
//            ConfigHelper.ISOFFLINEMODE=offline;
//            Intent intent=new Intent(this, MainActivity.class);
//            intent.putExtra("LOGIN_STATE",offline?"Offline":"AboutToLogin");
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
//                    Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }
//        else
//        {
//            Intent intent=new Intent(this, StartActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
//                    Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }
    }
}
