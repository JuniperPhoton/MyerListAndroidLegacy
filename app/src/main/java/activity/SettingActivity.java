package activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import java.util.Locale;

import helper.AppHelper;
import helper.ConfigHelper;
import helper.ContextUtil;
import moe.feng.material.statusbar.StatusBarCompat;

public class SettingActivity extends ActionBarActivity
{
    private com.rey.material.widget.Switch mShowKeyboardSwitch;
    private com.rey.material.widget.Switch mAddToBottomSwitch;
    private com.rey.material.widget.Switch mHandHobbitSwitch;
    private TextView mLangText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        //        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
        //        {
        //            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //        }
        setContentView(R.layout.activity_setting);

        mShowKeyboardSwitch = (com.rey.material.widget.Switch) findViewById(R.id.ShowKeyboardSwitch);
        mAddToBottomSwitch = (com.rey.material.widget.Switch) findViewById(R.id.AddToBottomSwitch);
        mHandHobbitSwitch = (com.rey.material.widget.Switch) findViewById(R.id.hand_hobbit_switch);

        mLangText = (TextView) findViewById(R.id.lang_btn);

        Boolean showKeyboard = ConfigHelper.getBoolean(ContextUtil.getInstance(), "ShowKeyboard");
        mShowKeyboardSwitch.setChecked(showKeyboard);

        Boolean addToBottom = ConfigHelper.getBoolean(ContextUtil.getInstance(), "AddToBottom");
        mAddToBottomSwitch.setChecked(addToBottom);

        Boolean handUse = ConfigHelper.getBoolean(ContextUtil.getInstance(), "HandHobbit");
        mHandHobbitSwitch.setChecked(handUse);

        final String langStr = ConfigHelper.getString(ContextUtil.getInstance(), "Language");
        if (langStr.equals("Chinese"))
        {
            mLangText.setText(getString(R.string.chinese));
        } else
            mLangText.setText("English");

        mLangText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle(getString(R.string.change_lang));
                //                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                //                {
                //                    @Override
                //                    public void onClick(DialogInterface dialogInterface, int i)
                //                    {
                //                        dialogInterface.dismiss();
                //                    }
                //                });
                builder.setSingleChoiceItems(new String[]{"English", getString(R.string.chinese)}, langStr.equals("Chinese") ? 1 : 0, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (i == 1)
                        {
                            Resources resources = getResources();
                            Configuration config = resources.getConfiguration();
                            DisplayMetrics dm = resources.getDisplayMetrics();
                            config.locale = Locale.CHINESE;
                            resources.updateConfiguration(config, dm);
                            ConfigHelper.putString(ContextUtil.getInstance(), "Language", "Chinese");
                        } else
                        {
                            Resources resources = getResources();
                            Configuration config = resources.getConfiguration();
                            DisplayMetrics dm = resources.getDisplayMetrics();
                            config.locale = Locale.ENGLISH;
                            resources.updateConfiguration(config, dm);
                            ConfigHelper.putString(ContextUtil.getInstance(), "Language", "English");
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

        mShowKeyboardSwitch.setOnCheckedChangeListener(new com.rey.material.widget.Switch.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(com.rey.material.widget.Switch aSwitch, boolean b)
            {
                ConfigHelper.putBoolean(ContextUtil.getInstance(), "ShowKeyboard", b);
            }
        });

        mAddToBottomSwitch.setOnCheckedChangeListener(new com.rey.material.widget.Switch.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(com.rey.material.widget.Switch aSwitch, boolean b)
            {
                ConfigHelper.putBoolean(ContextUtil.getInstance(), "AddToBottom", b);
            }

        });

        mHandHobbitSwitch.setOnCheckedChangeListener(new com.rey.material.widget.Switch.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(com.rey.material.widget.Switch aSwitch, boolean b)
            {
                ConfigHelper.putBoolean(ContextUtil.getInstance(), "HandHobbit", b);
                AppHelper.ShowShortToast(getResources().getString(R.string.rebootHint));
            }
        });


    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
