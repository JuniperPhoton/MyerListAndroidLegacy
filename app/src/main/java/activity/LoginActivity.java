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


public class LoginActivity extends AppCompatActivity {
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

    public void login_Click(View view) throws NoSuchAlgorithmException {
        if (!isDataValid()) {
            return;
        }
        //login directly
        if (!isToRegister) {

            progressDialog.setMessage(getResources().getString(R.string.loading_hint));
            progressDialog.show();

            CloudServices.checkExist(mEmailBox.getText().toString(), new IRequestCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    onCheckEmailResponse(response);
                }
            });
        }
        else {
            progressDialog.setMessage(getResources().getString(R.string.loading_hint));
            progressDialog.show();
            CloudServices.register(mEmailBox.getText().toString(),
                    mPasswordBox.getText().toString(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            onRegisteredResponse(jsonObject);
                        }
                    });
        }
    }

    private boolean isDataValid() {
        if (DataHelper.isStringNullOrEmpty(mEmailBox.getText().toString())) {
            ToastService.sendToast(getString(R.string.hint_input_email));
            return false;
        }
        if (!DataHelper.isEmailFormat(mEmailBox.getText().toString())) {
            ToastService.sendToast(getString(R.string.hint_email_not_invalid));
            return false;
        }
        if (DataHelper.isStringNullOrEmpty(mPasswordBox.getText().toString())) {
            ToastService.sendToast(getString(R.string.hint_input_psd));
            return false;
        }
        if (isToRegister && DataHelper.isStringNullOrEmpty(mConfirmPsBox.getText().toString())) {
            ToastService.sendToast(getString(R.string.hint_input_repsd));
            return false;
        }
        if (isToRegister && !(mConfirmPsBox.getText().toString().equals(mPasswordBox.getText().toString()))) {
            ToastService.sendToast(getString(R.string.hint_psd_not_match));
            return false;
        }
        return true;
    }


    private void onCheckEmailResponse(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                boolean isExist = response.getBoolean("isExist");
                if (isExist) {
                    CloudServices.getSalt(mEmailBox.getText().toString(),
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onGotSaltResponse(jsonObject);
                                }
                            });
                }
                else {
                    ToastService.sendToast(getResources().getString(R.string.hint_email_not_exist));
                }
            }
            else {
                ToastService.sendToast(getResources().getString(R.string.hint_email_not_exist));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
        catch (APIException e) {
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
            progressDialog.dismiss();
        }
    }

    //获得 Salt 后并登录
    private void onGotSaltResponse(final JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                String salt = response.getString("Salt");

                if (!DataHelper.isStringNullOrEmpty(salt)) {

                    CloudServices.login(mEmailBox.getText().toString(),
                            mPasswordBox.getText().toString(),
                            salt,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onLoginResponse(jsonObject);
                                }
                            });
                }
                else throw new IllegalArgumentException();
            }
            else throw new IllegalArgumentException();
        }
        catch (APIException e) {
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
            progressDialog.dismiss();
        }
        catch (Exception e) {
            e.printStackTrace();
            ToastService.sendToast(getResources().getString(R.string.hint_login_fail));
            progressDialog.dismiss();
        }
    }

    //发出登录请求之后
    private void onLoginResponse(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess;
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

                    ToastService.sendToast(getResources().getString(R.string.login_success));

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("LOGIN_STATE", "Logined");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else throw new IllegalArgumentException();
            }
            else {
                ToastService.sendToast(getResources().getString(R.string.hint_wrong_psd));
            }
        }
        catch (APIException e) {
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
        }
        catch (Exception e) {
            e.printStackTrace();
            ToastService.sendToast(getResources().getString(R.string.hint_login_fail));
        }
        finally {
            progressDialog.dismiss();
        }
    }

    //发出注册请求之后
    private void onRegisteredResponse(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess;
            isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                JSONObject userObj = response.getJSONObject("UserInfo");
                if (userObj != null) {
                    String salt = userObj.getString("Salt");
                    ConfigHelper.putString(AppExtension.getInstance(),
                            "email",
                            mEmailBox.getText().toString());
                    ConfigHelper.putString(AppExtension.getInstance(),
                            "password",
                            mPasswordBox.getText().toString());

                    CloudServices.login(ConfigHelper.getString(this, "email"),
                            ConfigHelper.getString(this, "password"),
                            salt,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onLoginResponse(jsonObject);
                                }
                            });
                }
            }
            else {
                double code=response.getDouble("error_code");
                if(code==203){
                    ToastService.sendToast(getResources().getString(R.string.hint_email_exist));
                }
            }
        }
        catch (APIException e) {
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
        }
        catch (Exception e) {
            e.printStackTrace();
            ToastService.sendToast(getResources().getString(R.string.hint_register_fail));
        }
        finally {
            progressDialog.dismiss();
        }
    }
}
