package activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.juniper.myerlistandroid.R;

public class SettingActivity extends ActionBarActivity
{
    private Switch mKeyboardSwitch;
    private Switch mAddToBottomSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mKeyboardSwitch=(Switch)findViewById(R.id.ShowKeyboardSwitch);
        mAddToBottomSwitch=(Switch)findViewById(R.id.AddToBottomSwitch);

        mKeyboardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {

            }
        });

        mAddToBottomSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {

            }
        });
    }

}
