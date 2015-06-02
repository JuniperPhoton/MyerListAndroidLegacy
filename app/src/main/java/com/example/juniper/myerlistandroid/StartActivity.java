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


public class StartActivity extends AppCompatActivity
{

    public final static String EXTRA_MESSAGE = "ToLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_start);
    }

    public void toLoginClick(View v)
    {

        Intent intent=new Intent(this,LoginActivity.class);
        intent.putExtra(EXTRA_MESSAGE, true);
        startActivity(intent);
    }

    public void toRegisterClick(View view)
    {
        Intent intent=new Intent(this,LoginActivity.class);
        intent.putExtra(EXTRA_MESSAGE,false);
        startActivity(intent);
    }

    public void toMainClick(View view)
    {
        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE,false);
        startActivity(intent);
    }


}