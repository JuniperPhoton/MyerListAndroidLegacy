package activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.juniperphoton.jputils.DataHelper;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.NetworkSecurityHelper;
import com.juniperphoton.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import api.CloudServices;
import exception.APIException;
import common.AppExtension;
import interfaces.IRequestCallback;
import moe.feng.material.statusbar.StatusBarCompat;
import util.ToastService;


public class LoginActivity extends AppCompatActivity {
    private final boolean DEBUG_ENABLE = true;

    private EditText mEmailBox;
    private EditText mPasswordBox;
    private EditText mConfirmPsBox;
    private TextView mTitleView;
    private ProgressDialog mprogressDialog;
    private TextView mForgetPwdTextView;

    private boolean isToRegister = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);

        setContentView(R.layout.activity_login);

        mEmailBox = (EditText) findViewById(R.id.activity_login_email_tv);
        mPasswordBox = (EditText) findViewById(R.id.activity_login_ps_et);

        if (DEBUG_ENABLE) {
            mEmailBox.setText("dengweichao@hotmail.com");
            mPasswordBox.setText("windfantasy");
        }

        mConfirmPsBox = (EditText) findViewById(R.id.activity_login_rps_et);
        mTitleView = (TextView) findViewById(R.id.activity_login_loginTitle_tv);
        mForgetPwdTextView = (TextView) findViewById(R.id.activity_login_forget_tv);
        mForgetPwdTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(getResources().getString(R.string.forget_pwd_title));
                builder.setMessage(getResources().getString(R.string.forget_pwd_content));
                builder.setPositiveButton(getResources().getString(R.string.forget_pwd_send_email), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dengweichao@hotmail.com"}); // recipients
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Change my password in MyerList");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                        startActivity(Intent.createChooser(emailIntent, "Choose app to send an email"));
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.forget_pwd_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        Intent intent = getIntent();
        String state = intent.getStringExtra("LOGIN_STATE");
        if (state.equals("ToLogin")) {
            mTitleView.setText(getResources().getString(R.string.loginBtn));
            mConfirmPsBox.setVisibility(View.GONE);
            isToRegister = false;
        } else
            mTitleView.setText(getResources().getString(R.string.registerBtn));

        mprogressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
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

            mprogressDialog.setMessage(getResources().getString(R.string.loading_hint));
            mprogressDialog.show();

            CloudServices.checkExist(mEmailBox.getText().toString(), new IRequestCallback() {
                @Override
                public void onResponse(JSONObject response) {
                    onCheckEmailResponse(response);
                }
            });
        } else {
            mprogressDialog.setMessage(getResources().getString(R.string.loading_hint));
            mprogressDialog.show();
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
            ToastService.sendShortToast(getString(R.string.hint_input_email));
            return false;
        }
        if (!DataHelper.isEmailFormat(mEmailBox.getText().toString())) {
            ToastService.sendShortToast(getString(R.string.hint_email_not_invalid));
            return false;
        }
        if (DataHelper.isStringNullOrEmpty(mPasswordBox.getText().toString())) {
            ToastService.sendShortToast(getString(R.string.hint_input_psd));
            return false;
        }
        if (isToRegister && DataHelper.isStringNullOrEmpty(mConfirmPsBox.getText().toString())) {
            ToastService.sendShortToast(getString(R.string.hint_input_repsd));
            return false;
        }
        if (isToRegister && !(mConfirmPsBox.getText().toString().equals(mPasswordBox.getText().toString()))) {
            ToastService.sendShortToast(getString(R.string.hint_psd_not_match));
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
                } else {
                    ToastService.sendShortToast(getResources().getString(R.string.hint_email_not_exist));
                }
            } else {
                ToastService.sendShortToast(getResources().getString(R.string.hint_email_not_exist));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            mprogressDialog.dismiss();
        } catch (APIException e) {
            ToastService.sendShortToast(getResources().getString(R.string.hint_request_fail));
            mprogressDialog.dismiss();
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

                    String psAfterMD5 = NetworkSecurityHelper.get32MD5Str(mPasswordBox.getText().toString());
                    String psToPost = NetworkSecurityHelper.get32MD5Str(psAfterMD5 + salt);

                    CloudServices.login(mEmailBox.getText().toString(),
                            psToPost,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onLoginResponse(jsonObject);
                                }
                            });
                } else throw new IllegalArgumentException();
            } else throw new IllegalArgumentException();
        } catch (APIException e) {
            ToastService.sendShortToast(getResources().getString(R.string.hint_request_fail));
            mprogressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
            ToastService.sendShortToast(getResources().getString(R.string.hint_login_fail));
            mprogressDialog.dismiss();
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
                    LocalSettingHelper.putString(AppExtension.getInstance(), "email", mEmailBox.getText().toString());
                    LocalSettingHelper.putString(AppExtension.getInstance(), "sid", sid);
                    LocalSettingHelper.putString(AppExtension.getInstance(), "access_token", access_token);
                    LocalSettingHelper.deleteKey(AppExtension.getInstance(), "password");

                    ToastService.sendShortToast(getResources().getString(R.string.login_success));

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("LOGIN_STATE", "Logined");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else throw new IllegalArgumentException();
            } else {
                ToastService.sendShortToast(getResources().getString(R.string.hint_wrong_psd));
            }
        } catch (APIException e) {
            ToastService.sendShortToast(getResources().getString(R.string.hint_request_fail));
        } catch (Exception e) {
            e.printStackTrace();
            ToastService.sendShortToast(getResources().getString(R.string.hint_login_fail));
        } finally {
            mprogressDialog.dismiss();
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

                    String psAfterMD5 = NetworkSecurityHelper.get32MD5Str(mPasswordBox.getText().toString());
                    String psToPost = NetworkSecurityHelper.get32MD5Str(psAfterMD5 + salt);

                    LocalSettingHelper.putString(AppExtension.getInstance(),
                            "email",
                            mEmailBox.getText().toString());
                    LocalSettingHelper.putString(AppExtension.getInstance(),
                            "password",
                            psToPost);

                    CloudServices.login(LocalSettingHelper.getString(this, "email"),
                            psToPost,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onLoginResponse(jsonObject);
                                }
                            });
                }
            } else {
                double code = response.getDouble("error_code");
                if (code == 203) {
                    ToastService.sendShortToast(getResources().getString(R.string.hint_email_exist));
                }
            }
        } catch (APIException e) {
            ToastService.sendShortToast(getResources().getString(R.string.hint_request_fail));
        } catch (Exception e) {
            e.printStackTrace();
            ToastService.sendShortToast(getResources().getString(R.string.hint_register_fail));
        } finally {
            mprogressDialog.dismiss();
        }
    }
}
