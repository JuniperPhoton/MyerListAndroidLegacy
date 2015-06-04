package helper;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.io.IOException;
import java.net.URI;

import model.OnActionListener;

public class PostHelper
{
    public static boolean isConnnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivityManager)
        {
            NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();

            if (null != networkInfo) {
                for (NetworkInfo info : networkInfo) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                       // Log.e(TAG, "the net is ok");
                        return true;
                    }
                }
            }
        }
        Toast.makeText(context, "Please check your network ;-)", Toast.LENGTH_SHORT).show();
        return false;
    }

    public final static String domain = "121.41.21.21";
    public static String UserCheckExist = "http://" + domain + "/schedule/User/CheckUserExist/v1?";
    public static String UserRegisterUri = "http://" + domain + "/schedule/User/Register/v1?";
    public static String UserLoginUri = "http://" + domain + "/schedule/User/Login/v1?";
    public static String UserGetSalt = "http://" + domain + "/schedule/User/GetSalt/v1";
    public static String ScheduleAddUri = "http://" + domain + "/schedule/Schedule/AddSchedule/v1?";
    public static String ScheduleUpdateUri = "http://" + domain + "/schedule/Schedule/UpdateContent/v1?";
    public static String ScheduleFinishUri = "http://" + domain + "/schedule/Schedule/FinishSchedule/v1?";
    public static String ScheduleDeleteUri = "http://" + domain + "/schedule/Schedule/DeleteSchedule/v1?";
    public static String ScheduleGetUri = "http://" + domain + "/schedule/Schedule/GetMySchedules/v1?";
    public static String ScheduleGetOrderUri = "http://" + domain + "/schedule/Schedule/GetMyOrder/v1?";
    public static String ScheduleSetOrderUri = "http://" + domain + "/schedule/Schedule/SetMyOrder/v1?";

    private static boolean mExist=false;
    public static OnCheckResponseListener mOnCheckResponseListener;
    public static OnGetSaltResponseListener mOnGetSaltResponseListener;
    public static OnLoginResponseListener mOnLoginResponseListener;
    public static OnActionListener mOnActionListener;

    public  static void CheckExist(Context context,final String email)
    {
        mOnCheckResponseListener=(OnCheckResponseListener)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams requestParams=new RequestParams();
        requestParams.add("email", email);
        client.post(UserCheckExist, requestParams,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                JSONObject jsonObject=response;
                try
                {
                    boolean isSuccess=jsonObject.getBoolean("isSuccessed");
                    if(isSuccess)
                    {
                        boolean isExist=jsonObject.getBoolean("isExist");
                        if(isExist)
                        {
                            mOnCheckResponseListener.OnCheckResponse(true);
                        }
                        else mOnCheckResponseListener.OnCheckResponse(false);
                    }
                    else mOnCheckResponseListener.OnCheckResponse(false);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnCheckResponseListener.OnCheckResponse(false);
                }
            }

        });
    }

    public static void GetSalt(Context context,String email)
    {
        mOnGetSaltResponseListener=(OnGetSaltResponseListener)context;
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("email", email);
        client.post(UserGetSalt,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                JSONObject jsonObject=response;
                try
                {
                    boolean isSuccess=jsonObject.getBoolean("isSuccessed");
                    if(isSuccess)
                    {
                        String salt=jsonObject.getString("Salt");
                        mOnGetSaltResponseListener.OnGetSaltResponse(salt);
                    }
                    else mOnGetSaltResponseListener.OnGetSaltResponse(null);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    try
                    {
                        mOnGetSaltResponseListener.OnGetSaltResponse(null);
                    }
                    catch (NoSuchAlgorithmException e1)
                    {
                        e1.printStackTrace();
                    }
                }
                catch (NoSuchAlgorithmException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void Login(Context context, final String email, final String password,String salt) throws NoSuchAlgorithmException
    {
        mOnLoginResponseListener=(OnLoginResponseListener)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();

        String ps=password;
        String psAfterMD5=NetworkSecurityHelper.get32MD5Str(ps);
        String psToPost=NetworkSecurityHelper.get32MD5Str(psAfterMD5 + salt);

        params.put("email",email);
        params.put("password",psToPost);

        client.post(UserLoginUri,params,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                boolean isSuccess= false;
                try
                {
                    isSuccess = response.getBoolean("isSuccessed");
                    if(isSuccess)
                    {
                        JSONObject userObj=response.getJSONObject("UserInfo");
                        if(userObj!=null)
                        {
                            String sid=userObj.getString("sid");
                            String access_token=userObj.getString("access_token");
                            ConfigHelper.putString(ContextUtil.getInstance(),"email",email);
                            ConfigHelper.putString(ContextUtil.getInstance(),"password",password);
                            ConfigHelper.putString(ContextUtil.getInstance(),"sid",sid);
                            ConfigHelper.putString(ContextUtil.getInstance(),"access_token",access_token);
                            mOnLoginResponseListener.OnLoginResponse(true);
                        }
                        else mOnLoginResponseListener.OnLoginResponse(false);
                    }
                    else mOnLoginResponseListener.OnLoginResponse((false));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnLoginResponseListener.OnLoginResponse((false));
                }

            }

        });
    }

    public interface OnCheckResponseListener
    {
        void OnCheckResponse(boolean check);
    }

    public interface OnGetSaltResponseListener
    {
        void OnGetSaltResponse(String str) throws NoSuchAlgorithmException;
    }

    public interface OnLoginResponseListener
    {
        void OnLoginResponse(boolean value);
    }
}
