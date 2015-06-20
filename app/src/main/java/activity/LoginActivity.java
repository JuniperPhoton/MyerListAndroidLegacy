package activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;

import java.security.NoSuchAlgorithmException;

import helper.AppHelper;
import helper.ConfigHelper;
import helper.DataHelper;
import helper.PostHelper;


public class LoginActivity extends AppCompatActivity implements PostHelper.OnCheckResponseListener,PostHelper.OnGetSaltResponseListener,PostHelper.OnLoginResponseListener,PostHelper.OnRegisterListener
{
    private EditText mEmailBox;
    private EditText mPasswordBox;
    private EditText mConfirmPsBox;
    private TextView mTitleView;
    private ProgressDialog progressDialog;

    private boolean isToRegister=true;

    private final boolean DEBUG_ENABLE=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_login);

        mEmailBox=(EditText)findViewById(R.id.emailbox);
        mPasswordBox=(EditText)findViewById(R.id.psbox);
        mConfirmPsBox=(EditText)findViewById(R.id.confirmpsbox);
        mTitleView=(TextView)findViewById(R.id.logintitle);

        Intent intent=getIntent();
        String state=intent.getStringExtra("LOGIN_STATE");
        if(state.equals("ToLogin"))
        {
            mTitleView.setText(getResources().getString(R.string.loginBtn));
            mConfirmPsBox.setVisibility(View.GONE);
            isToRegister=false;
        }
        else mTitleView.setText(getResources().getString(R.string.registerBtn));

        progressDialog=new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);


    }

    public void loginClick(View view) throws NoSuchAlgorithmException
    {

        if(!isToRegister)
        {
            if(!DataHelper.IsStringNullOrEmpty(mEmailBox.getText().toString()))
            {
                if(DataHelper.IsEmailFormat(mEmailBox.getText().toString()))
                {
                    if(!DataHelper.IsStringNullOrEmpty(mPasswordBox.getText().toString()))
                    {
                        progressDialog.setMessage(getResources().getString(R.string.loading_hint));
                        progressDialog.show();
                        PostHelper.CheckExist(this,mEmailBox.getText().toString());
                    }
                    else AppHelper.ShowShortToast("Please input password");
                }
                else AppHelper.ShowShortToast("Please input valid email");
            }
        }
        else if(isToRegister)
        {
            if(!DataHelper.IsStringNullOrEmpty(mEmailBox.getText().toString()))
            {
                if(DataHelper.IsEmailFormat(mEmailBox.getText().toString()))
                {
                    if(!DataHelper.IsStringNullOrEmpty(mPasswordBox.getText().toString()))
                    {
                        if(!DataHelper.IsStringNullOrEmpty(mConfirmPsBox.getText().toString()))
                        {
                            if(mConfirmPsBox.getText().toString().equals(mPasswordBox.getText().toString()))
                            {
                                progressDialog.setMessage(getResources().getString(R.string.loading_hint));
                                progressDialog.show();
                                PostHelper.Register(this, mEmailBox.getText().toString(),mPasswordBox.getText().toString());
                            }
                            else AppHelper.ShowShortToast(getString(R.string.two_ps_match));
                        }
                        else AppHelper.ShowShortToast(getString(R.string.confirm_ps_lost));
                    }
                    else AppHelper.ShowShortToast(getString(R.string.ps_lost));
                }
                else AppHelper.ShowShortToast(getString(R.string.email_invalid));
            }
        }
    }


    @Override
    public void OnCheckResponse(boolean check)
    {
        if(check)
        {
            PostHelper.GetSalt(this,mEmailBox.getText().toString());
        }
        else AppHelper.ShowShortToast(getResources().getString(R.string.user_dont_exist));

        progressDialog.dismiss();
    }

    @Override
    public void OnGetSaltResponse(String str) throws NoSuchAlgorithmException
    {
        String salt=str;
        if(!DataHelper.IsStringNullOrEmpty(salt))
        {
            PostHelper.Login(this,mEmailBox.getText().toString(),mPasswordBox.getText().toString(),salt);
        }
        else AppHelper.ShowShortToast(getResources().getString(R.string.fail_to_login));

        progressDialog.dismiss();
    }

    @Override
    public void OnLoginResponse(boolean value)
    {
        if(value)
        {
            AppHelper.ShowShortToast(getResources().getString(R.string.login_success));

            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("LOGIN_STATE","Logined");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else AppHelper.ShowShortToast(getResources().getString(R.string.fail_to_login));

        progressDialog.dismiss();
    }

    @Override
    public void OnRegisteredResponse(boolean isSuccess, String salt)
    {
        if(isSuccess)
        {
            try
            {
                PostHelper.Login(this, ConfigHelper.getString(this, "email"), ConfigHelper.getString(this, "password"), ConfigHelper.getString(this, "salt"));
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
        }
        else AppHelper.ShowShortToast(getResources().getString(R.string.fail_to_register));

        progressDialog.dismiss();
    }
}
