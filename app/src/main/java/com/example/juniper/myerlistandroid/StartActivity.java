package com.example.juniper.myerlistandroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import helper.ConfigHelper;
import helper.PostHelper;


public class StartActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_start);

        String email=ConfigHelper.getString(this,"email");
        boolean offline=ConfigHelper.getBoolean(this,"offline_mode");
        if(offline || email!=null )
        {
            toMainClick(null);
        }

    }

    public void toLoginClick(View v)
    {
        Intent intent=new Intent(this,LoginActivity.class);
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