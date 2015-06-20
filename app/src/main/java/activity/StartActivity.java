package activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.juniper.myerlistandroid.R;

import activity.LoginActivity;
import activity.MainActivity;
import helper.ConfigHelper;


public class StartActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_start);

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP)
        {
            CardView toLoginCard=(CardView)findViewById(R.id.toLoginBtn_cardview);
            LinearLayout.LayoutParams layoutParamsForTop=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsForTop.setMargins(40,60,40,0);
            layoutParamsForTop.height=100;
            toLoginCard.setLayoutParams(layoutParamsForTop);

            CardView toRegisterCard=(CardView)findViewById(R.id.toRegisterBtn_cardview);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(40,0,40,0);
            layoutParams.height=100;
            toRegisterCard.setLayoutParams(layoutParams);

            CardView toMainCard=(CardView)findViewById(R.id.toMainBtn_cardview);
            LinearLayout.LayoutParams layoutParamsForBottom=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsForBottom.setMargins(40,0,40,10);
            layoutParamsForBottom.height=100;
            toMainCard.setLayoutParams(layoutParamsForBottom);
        }
    }

    public void toLoginClick(View v)
    {
        Intent intent=new Intent(this, LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToLogin");
        startActivity(intent);
    }

    public void toRegisterClick(View view)
    {
        Intent intent=new Intent(this,LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToRegister");
        startActivity(intent);
    }

    public void toMainClick(View view)
    {
        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("LOGIN_STATE","OfflineMode");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        ConfigHelper.putBoolean(this, "offline_mode", true);
    }
}