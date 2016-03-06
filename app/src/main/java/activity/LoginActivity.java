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

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import api.CloudServices;
import exception.APIException;
import util.AppExtension;
import util.ConfigHelper;
import util.DataHelper;
import interfaces.IRequestCallback;
import moe.feng.material.statusbar.StatusBarCompat;
import util.ToastService;


public class LoginActivity extends AppCompatActivity{
    private EditText mEmailBox;
    private EditText mPasswordBox;
    private EditText mConfirmPsBox;
    private TextView mTitleView;
    private ProgressDialog progressDialog;
    private ImageView mMaskView;

    private boolean isToRegister = true;

    private final boolean DEBUG_ENABLE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_login);

        mMaskView = (ImageView) findViewById(R.id.activity_login_mask);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mMaskView.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
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
        if (state.equals("ToLogin")) {
            mTitleView.setText(getResources().getString(R.string.loginBtn));
            mConfirmPsBox.setVisibility(View.GONE);
            isToRegister = false;
        }
        else
            mTitleView.setText(getResources().getString(R.string.registerBtn));

        progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);


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

    public void Login_Click(View view) throws NoSuchAlgorithmException {
        if (!IsDataValid()) {
            return;
        }
        //Login directly
        if (!isToRegister) {

            progressDialog.setMessage(getResources().getString(R.string.loading_hint));
            progressDialog.show();

            CloudServices.CheckExist(mEmailBox.getText().toString(), new IRequestCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    onCheckEmailResponse(response);
                }
            });
        }
        else {
            progressDialog.setMessage(getResources().getString(R.string.loading_hint));
            progressDialog.show();
            CloudServices.Register(mEmailBox.getText().toString(),
                    mPasswordBox.getText().toString(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            onRegisteredResponse(jsonObject);
                        }
                    });
        }
    }

    private boolean IsDataValid() {
        if (DataHelper.IsStringNullOrEmpty(mEmailBox.getText().toString())) {
            return false;
        }
        if (DataHelper.IsEmailFormat(mEmailBox.getText().toString())) {
            return false;
        }
        if (!DataHelper.IsStringNullOrEmpty(mPasswordBox.getText().toString())) {
            return false;
        }
        if (isToRegister && !DataHelper.IsStringNullOrEmpty(mConfirmPsBox.getText().toString())) {
            return false;
        }
        if (isToRegister && mConfirmPsBox.getText().toString().equals(mPasswordBox.getText().toString())) {

            return false;
        }
        return true;
    }

    //检查邮件有效性，
    //然后获得 Salt 后登录
    private void onCheckEmailResponse(JSONObject response) {
        try {
            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                boolean isExist = response.getBoolean("isExist");
                if(isExist){
                    CloudServices.GetSalt(mEmailBox.getText().toString(),
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onGotSaltResponse(jsonObject);
                                }
                            });
                }
                else {
                    ToastService.ShowShortToast(getResources().getString(R.string.user_dont_exist));
                }
            }
            else throw new APIException();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (APIException e){
            ToastService.ShowShortToast(getResources().getString(R.string.fail_to_login));
        }
        progressDialog.dismiss();
    }

    //获得 Salt 后并登录
    private void onGotSaltResponse(final JSONObject response) {
        try {
            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                String salt = response.getString("Salt");

                if (!DataHelper.IsStringNullOrEmpty(salt)) {
                    try {
                        CloudServices.Login(mEmailBox.getText().toString(),
                                mPasswordBox.getText().toString(), salt,
                                new IRequestCallback() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        onLoginResponse(response);
                                    }
                                });
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                    ToastService.ShowShortToast(getResources().getString(R.string.fail_to_login));

                progressDialog.dismiss();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    //发出登录请求之后
    private void onLoginResponse(JSONObject response) {
        boolean isSuccess = false;
        try {
            isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                JSONObject userObj = response.getJSONObject("UserInfo");
                if (userObj != null) {
                    String sid = userObj.getString("sid");
                    String access_token = userObj.getString("access_token");
                    ConfigHelper.putString(AppExtension.getInstance(), "email", mEmailBox.getText().toString());
                    ConfigHelper.putString(AppExtension.getInstance(), "sid", sid);
                    ConfigHelper.putString(AppExtension.getInstance(), "access_token", access_token);
                    ConfigHelper.DeleteKey(AppExtension.getInstance(), "password");
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (isSuccess) {
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

    //发出注册请求之后
    private void onRegisteredResponse(JSONObject response) {
        boolean isSuccess = false;
        try {
            isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                JSONObject userObj = response.getJSONObject("UserInfo");
                if (userObj != null) {
                    String salt = userObj.getString("Salt");
                    ConfigHelper.putString(AppExtension.getInstance(), "email", mEmailBox.getText().toString());
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (isSuccess) {
            try {
                CloudServices.Login(ConfigHelper.getString(this, "email"),
                        ConfigHelper.getString(this, "password"),
                        ConfigHelper.getString(this, "salt"),
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                onLoginResponse(jsonObject);
                            }
                        });
            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        else
            ToastService.ShowShortToast(getResources().getString(R.string.fail_to_register));

        progressDialog.dismiss();
    }
}
