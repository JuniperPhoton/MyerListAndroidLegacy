package com.example.juniper.myerlistandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;

import helper.AppHelper;
import helper.DataHelper;
import helper.PostHelper;
import model.LoginState;


public class LoginActivity extends AppCompatActivity implements PostHelper.OnCheckResponseListener,PostHelper.OnGetSaltResponseListener,PostHelper.OnLoginResponseListener
{
    private EditText mEmailBox;
    private EditText mPasswordBox;
    private EditText mConfirmPsBox;
    private TextView mTitleView;
    private ProgressDialog progressDialog;

    private boolean isToRegister=true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailBox=(EditText)findViewById(R.id.emailbox);
        mPasswordBox=(EditText)findViewById(R.id.psbox);
        mConfirmPsBox=(EditText)findViewById(R.id.confirmpsbox);
        mTitleView=(TextView)findViewById(R.id.logintitle);

        Intent intent=getIntent();
        String state=intent.getStringExtra("LOGIN_STATE");
        if(state.equals("ToLogin"))
        {
            mTitleView.setText("LOGIN");
            mConfirmPsBox.setVisibility(View.GONE);
            isToRegister=false;
        }
        else mTitleView.setText("REGISTER");

        progressDialog=new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
    }

    public void loginClick(View view)
    {

        if(!DataHelper.IsStringNullOrEmpty(mEmailBox.getText().toString()))
        {
            if(DataHelper.IsEmailFormat(mEmailBox.getText().toString()))
            {
                if(!DataHelper.IsStringNullOrEmpty(mPasswordBox.getText().toString()))
                {
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                    PostHelper.CheckExist(this,mEmailBox.getText().toString());
                }
                else AppHelper.ShowShortToast("Please input password");
            }
            else AppHelper.ShowShortToast("Please input valid email");
        }
    }


    @Override
    public void OnCheckResponse(boolean check)
    {
        if(check)
        {
            PostHelper.GetSalt(this,mEmailBox.getText().toString());
        }
        else AppHelper.ShowShortToast("User do not exist.");
    }

    @Override
    public void OnGetSaltResponse(String str) throws NoSuchAlgorithmException
    {
        String salt=str;
        if(!DataHelper.IsStringNullOrEmpty(salt))
        {
            PostHelper.Login(this,mEmailBox.getText().toString(),mPasswordBox.getText().toString(),salt);
        }
        else AppHelper.ShowShortToast("Fail to login,please try again :-(");
    }

    @Override
    public void OnLoginResponse(boolean value)
    {
        if(value)
        {
            AppHelper.ShowShortToast("Successfully login!");

            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("LOGIN_STATE","Logined");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else AppHelper.ShowShortToast("Fail to login,please try again :-(");

        progressDialog.dismiss();
    }
}
