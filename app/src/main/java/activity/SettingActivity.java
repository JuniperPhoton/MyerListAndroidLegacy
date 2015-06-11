package activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.juniper.myerlistandroid.R;

import helper.ConfigHelper;
import helper.ContextUtil;

public class SettingActivity extends ActionBarActivity
{
    private Switch mShowKeyboardSwitch;
    private Switch mAddToBottomSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mShowKeyboardSwitch =(Switch)findViewById(R.id.ShowKeyboardSwitch);
        mAddToBottomSwitch=(Switch)findViewById(R.id.AddToBottomSwitch);

        Boolean showKeyboard=ConfigHelper.getBoolean(ContextUtil.getInstance(), "ShowKeyboard");
        mShowKeyboardSwitch.setChecked(showKeyboard);

        Boolean addToBottom=ConfigHelper.getBoolean(ContextUtil.getInstance(),"AddToBottom");
        mAddToBottomSwitch.setChecked(addToBottom);

        mShowKeyboardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                ConfigHelper.putBoolean(ContextUtil.getInstance(), "ShowKeyboard", b);
            }
        });

        mAddToBottomSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                ConfigHelper.putBoolean(ContextUtil.getInstance(),"AddToBottom",b);
            }
        });
    }

}
