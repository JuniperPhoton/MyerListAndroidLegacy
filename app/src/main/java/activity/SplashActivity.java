package activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.myerlistandroid.R;

import util.AppConfig;

/**
 * Created by dengw on 2016-07-01.
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                String access_token = LocalSettingHelper.getString(SplashActivity.this, "access_token");
                boolean offline = LocalSettingHelper.getBoolean(SplashActivity.this, "offline_mode");

                AppConfig.ISOFFLINEMODE = offline;

                //还没有登录/进入离线模式，回到 StartActivity
                if (!offline && access_token == null) {
                    AppConfig.ISOFFLINEMODE = false;
                    Intent intent = new Intent(SplashActivity.this, StartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
                finish();
            }
        }, 1000);

    }
}
