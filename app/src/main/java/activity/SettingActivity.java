package activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;

import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import common.App;
import moe.feng.material.statusbar.StatusBarCompat;
import util.GlobalListLocator;

public class SettingActivity extends AppCompatActivity {

    @Bind(R.id.activity_setting_addToEnd_s)
    com.rey.material.widget.Switch mAddToBottomSwitch;

    @Bind(R.id.activity_setting_language_tv)
    TextView mLangText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        Boolean addToBottom = LocalSettingHelper.getBoolean(App.getInstance(), "AddToBottom");
        mAddToBottomSwitch.setChecked(addToBottom);

        mAddToBottomSwitch.setOnCheckedChangeListener(new com.rey.material.widget.Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(com.rey.material.widget.Switch aSwitch, boolean b) {
                LocalSettingHelper.putBoolean(App.getInstance(), "AddToBottom", b);
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

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.activity_setting_language_tv)
    void onClickSetLanguage() {
        final String langStr = LocalSettingHelper.getString(App.getInstance(), "Language", "");
        if (langStr != null) {
            if (langStr.equals("Chinese")) {
                mLangText.setText(getString(R.string.chinese));
            } else {
                mLangText.setText(getString(R.string.english));
            }
        } else {
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            if (config.locale == Locale.CHINESE) {
                mLangText.setText(getString(R.string.chinese));
            } else {
                mLangText.setText(getString(R.string.english));
            }
        }

        int choice = langStr.equals("Chinese") ? 1 : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle(getString(R.string.change_lang));
        builder.setSingleChoiceItems(new String[]{"English", getString(R.string.chinese)},
                choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 1) {
                            Resources resources = getResources();
                            Configuration config = resources.getConfiguration();
                            DisplayMetrics dm = resources.getDisplayMetrics();
                            config.locale = Locale.CHINESE;
                            resources.updateConfiguration(config, dm);
                            LocalSettingHelper.putString(App.getInstance(), "Language", "Chinese");
                        } else {
                            Resources resources = getResources();
                            Configuration config = resources.getConfiguration();
                            DisplayMetrics dm = resources.getDisplayMetrics();
                            config.locale = Locale.ENGLISH;
                            resources.updateConfiguration(config, dm);
                            LocalSettingHelper.putString(App.getInstance(), "Language", "English");
                        }
                        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                        intent.putExtra("LOGIN_STATE", "AboutToLogin");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

        builder.create().show();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.activity_setting_logout_tv)
    void onClickLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_content);
        builder.setPositiveButton(getResources().getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LocalSettingHelper.putBoolean(getApplicationContext(), "offline_mode", false);
                LocalSettingHelper.deleteKey(getApplicationContext(), "email");
                LocalSettingHelper.deleteKey(getApplicationContext(), "salt");
                LocalSettingHelper.deleteKey(getApplicationContext(), "access_token");
                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                GlobalListLocator.clearData();
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
}
