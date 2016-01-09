package activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juniperphoton.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import util.ConfigHelper;
import util.DataHelper;
import util.PostHelper;
import interfaces.IRequestCallbacks;
import model.ToDo;
import moe.feng.material.statusbar.StatusBarCompat;
import util.ToastService;


public class LoginActivity extends AppCompatActivity implements
        IRequestCallbacks
{
    private EditText mEmailBox;
    private EditText mPasswordBox;
    private EditText mConfirmPsBox;
    private TextView mTitleView;
    private ProgressDialog progressDialog;
    private ImageView mMaskView;


    private boolean isToRegister = true;

    private final boolean DEBUG_ENABLE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_login);

        mMaskView = (ImageView) findViewById(R.id.activity_login_mask);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            mMaskView.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            CardView toLoginCard = (CardView) findViewById(R.id.activity_login_btn_cardView);
            LinearLayout.LayoutParams layoutParamsForTop = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsForTop.setMargins(40, 60, 40, 0);
            layoutParamsForTop.height = 140;
            toLoginCard.setLayoutParams(layoutParamsForTop);
        }


        mEmailBox = (EditText) findViewById(R.id.activity_login_emailText);
        mPasswordBox = (EditText) findViewById(R.id.activity_login_psText);
        mConfirmPsBox = (EditText) findViewById(R.id.activity_login_reInputPsText);
        mTitleView = (TextView) findViewById(R.id.logintitle);

        Intent intent = getIntent();
        String state = intent.getStringExtra("LOGIN_STATE");
        if (state.equals("ToLogin"))
        {
            mTitleView.setText(getResources().getString(R.string.loginBtn));
            mConfirmPsBox.setVisibility(View.GONE);
            isToRegister = false;
        }
        else
            mTitleView.setText(getResources().getString(R.string.registerBtn));

        progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);


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

    public void Login_Click(View view) throws NoSuchAlgorithmException
    {

        if (!isToRegister)
        {
            if (!DataHelper.IsStringNullOrEmpty(mEmailBox.getText().toString()))
            {
                if (DataHelper.IsEmailFormat(mEmailBox.getText().toString()))
                {
                    if (!DataHelper.IsStringNullOrEmpty(mPasswordBox.getText().toString()))
                    {
                        progressDialog.setMessage(getResources().getString(R.string.loading_hint));
                        progressDialog.show();
                        PostHelper.CheckExist(this, mEmailBox.getText().toString());
                    }
                    else
                        ToastService.ShowShortToast("Please input password");
                }
                else
                    ToastService.ShowShortToast("Please input valid email");
            }
        }
        else if (isToRegister)
        {
            if (!DataHelper.IsStringNullOrEmpty(mEmailBox.getText().toString()))
            {
                if (DataHelper.IsEmailFormat(mEmailBox.getText().toString()))
                {
                    if (!DataHelper.IsStringNullOrEmpty(mPasswordBox.getText().toString()))
                    {
                        if (!DataHelper.IsStringNullOrEmpty(mConfirmPsBox.getText().toString()))
                        {
                            if (mConfirmPsBox.getText().toString().equals(mPasswordBox.getText().toString()))
                            {
                                progressDialog.setMessage(getResources().getString(R.string.loading_hint));
                                progressDialog.show();
                                PostHelper.Register(this, mEmailBox.getText().toString(), mPasswordBox.getText().toString());
                            }
                            else
                                ToastService.ShowShortToast(getString(R.string.two_ps_match));
                        }
                        else
                            ToastService.ShowShortToast(getString(R.string.confirm_ps_lost));
                    }
                    else
                        ToastService.ShowShortToast(getString(R.string.ps_lost));
                }
                else
                    ToastService.ShowShortToast(getString(R.string.email_invalid));
            }
        }
    }


    @Override
    public void OnCheckResponse(boolean check)
    {
        if (check)
        {
            PostHelper.GetSalt(this, mEmailBox.getText().toString());
        }
        else
            ToastService.ShowShortToast(getResources().getString(R.string.user_dont_exist));

        progressDialog.dismiss();
    }

    @Override
    public void OnGetSaltResponse(String str)
    {
        String salt = str;
        if (!DataHelper.IsStringNullOrEmpty(salt))
        {
            try
            {
                PostHelper.Login(this, mEmailBox.getText().toString(), mPasswordBox.getText().toString(), salt);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
            ToastService.ShowShortToast(getResources().getString(R.string.fail_to_login));

        progressDialog.dismiss();
    }

    @Override
    public void OnLoginResponse(boolean value)
    {
        if (value)
        {
            ToastService.ShowShortToast(getResources().getString(R.string.login_success));

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("LOGIN_STATE", "Logined");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
            ToastService.ShowShortToast(getResources().getString(R.string.fail_to_login));

        progressDialog.dismiss();
    }

    @Override
    public void OnRegisteredResponse(boolean isSuccess, String salt)
    {
        if (isSuccess)
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
        else
            ToastService.ShowShortToast(getResources().getString(R.string.fail_to_register));

        progressDialog.dismiss();
    }

    @Override
    public void OnGotScheduleResponse(boolean isSuccess, ArrayList<ToDo> mytodosList)
    {

    }

    @Override
    public void OnAddedResponse(boolean isSuccess, ToDo newTodo)
    {

    }

    @Override
    public void OnSetOrderResponse(boolean isSuccess)
    {

    }


    @Override
    public void OnDoneResponse(boolean isSuccess)
    {

    }

    @Override
    public void OnDeleteResponse(boolean isSuccess)
    {

    }

    @Override
    public void OnUpdateContent(boolean isSuccess)
    {

    }
}
