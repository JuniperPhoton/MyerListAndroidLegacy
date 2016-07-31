package activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;
import com.juniperphoton.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import java.util.Locale;

import util.ConfigHelper;
import util.AppExtension;
import moe.feng.material.statusbar.StatusBarCompat;
import util.ToastService;

public class SettingActivity extends AppCompatActivity {
    private com.rey.material.widget.Switch mAddToBottomSwitch;
    private TextView mLangText;
    private TextView mLogoutBtn;
    private ImageView mMaskView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_setting);

        mMaskView = (ImageView) findViewById(R.id.activity_setting_mask_iv);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mMaskView.setVisibility(View.GONE);
        }

        //找到开关控件
        mAddToBottomSwitch = (com.rey.material.widget.Switch) findViewById(R.id.activity_setting_addToEnd_s);

        mLangText = (TextView) findViewById(R.id.activity_setting_language_tv);

        Boolean addToBottom = ConfigHelper.getBoolean(AppExtension.getInstance(), "AddToBottom");
        mAddToBottomSwitch.setChecked(addToBottom);

        //找到语言
        final String langStr = ConfigHelper.getString(AppExtension.getInstance(), "Language");
        if (langStr.equals("Chinese")) {
            mLangText.setText(getString(R.string.chinese));
        }
        else{
            mLangText.setText("English");
        }

        mLangText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle(getString(R.string.change_lang));
                builder.setSingleChoiceItems(new String[]{"English", getString(R.string.chinese)}, langStr.equals("Chinese") ? 1 : 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 1) {
                            Resources resources = getResources();
                            Configuration config = resources.getConfiguration();
                            DisplayMetrics dm = resources.getDisplayMetrics();
                            config.locale = Locale.CHINESE;
                            resources.updateConfiguration(config, dm);
                            ConfigHelper.putString(AppExtension.getInstance(), "Language", "Chinese");
                        }
                        else {
                            Resources resources = getResources();
                            Configuration config = resources.getConfiguration();
                            DisplayMetrics dm = resources.getDisplayMetrics();
                            config.locale = Locale.ENGLISH;
                            resources.updateConfiguration(config, dm);
                            ConfigHelper.putString(AppExtension.getInstance(), "Language", "English");
                        }
                        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                        intent.putExtra("LOGIN_STATE", "AboutToLogin");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

                builder.create().show();
            }
        });

        mLogoutBtn = (TextView) findViewById(R.id.activity_setting_logout_tv);
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle(R.string.logout_title);
                builder.setMessage(R.string.logout_content);
                builder.setPositiveButton(getResources().getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ConfigHelper.putBoolean(getApplicationContext(), "offline_mode", false);
                        ConfigHelper.DeleteKey(getApplicationContext(), "email");
                        ConfigHelper.DeleteKey(getApplicationContext(), "salt");
                        ConfigHelper.DeleteKey(getApplicationContext(), "access_token");
                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        mAddToBottomSwitch.setOnCheckedChangeListener(new com.rey.material.widget.Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(com.rey.material.widget.Switch aSwitch, boolean b) {
                ConfigHelper.putBoolean(AppExtension.getInstance(), "AddToBottom", b);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
