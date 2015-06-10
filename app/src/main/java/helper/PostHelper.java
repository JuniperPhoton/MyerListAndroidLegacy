package helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import model.Schedule;

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
    public static OnGetSchedulesListener mOnGetSchedules;
    public static OnAddedMemoListener mOnAddedListener;

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

    public static void Login(Context context, final String email, final String password, final String salt) throws NoSuchAlgorithmException
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
                boolean isSuccess = false;
                try
                {
                    isSuccess = response.getBoolean("isSuccessed");
                    if (isSuccess)
                    {
                        JSONObject userObj = response.getJSONObject("UserInfo");
                        if (userObj != null)
                        {
                            String sid = userObj.getString("sid");
                            String access_token = userObj.getString("access_token");
                            ConfigHelper.putString(ContextUtil.getInstance(), "email", email);
                            ConfigHelper.putString(ContextUtil.getInstance(), "password", password);
                            ConfigHelper.putString(ContextUtil.getInstance(),"salt",salt);
                            ConfigHelper.putString(ContextUtil.getInstance(), "sid", sid);
                            ConfigHelper.putString(ContextUtil.getInstance(), "access_token", access_token);
                            mOnLoginResponseListener.OnLoginResponse(true);
                        } else mOnLoginResponseListener.OnLoginResponse(false);
                    } else mOnLoginResponseListener.OnLoginResponse((false));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnLoginResponseListener.OnLoginResponse((false));
                }

            }
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object)
            {
                mOnLoginResponseListener.OnLoginResponse((false));
            }

        });
    }

    public static void GetOrderedSchedules(Context context,final String sid, final String access_token)
    {
        mOnGetSchedules=(OnGetSchedulesListener)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("sid",sid);

        client.post(ScheduleGetUri+"sid="+sid+"&access_token="+access_token, params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                Boolean isSuccess = null;
                try
                {
                    isSuccess = response.getBoolean("isSuccessed");
                    if(isSuccess)
                    {
                        JSONArray array=response.getJSONArray("ScheduleInfo");

                        if(array!=null)
                        {
                            final List<Schedule> todosList=Schedule.parseJsonObjFromArray(array);

                            AsyncHttpClient client=new AsyncHttpClient();
                            RequestParams params=new RequestParams();
                            params.put("sid",sid);
                            client.post(ScheduleGetOrderUri+"sid="+sid+"&access_token="+access_token,params,new JsonHttpResponseHandler()
                            {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                                {
                                    try
                                    {
                                        boolean isSuccess=response.getBoolean("isSuccessed");
                                        if(isSuccess)
                                        {
                                            String orderStr=response.getJSONArray(("OrderList")).getJSONObject(0).getString("list_order");
                                            List<Schedule> listToReturn=Schedule.setOrderByString(todosList,orderStr);
                                            mOnGetSchedules.OnGotScheduleResponse(listToReturn);
                                        }
                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }

        });
    }

    public static void AddMemo(Context context,String sid,String content,String isDone)
    {
        mOnAddedListener=(OnAddedMemoListener)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("sid",sid);
        params.put("time", Calendar.getInstance().getTime().toString());
        params.put("content",content);
        params.put("isdone",isDone);
        client.post(ScheduleAddUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context,"access_token"), params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                Boolean isSuccess = null;
                try
                {
                    isSuccess = response.getBoolean("isSuccessed");
                    if (isSuccess)
                    {
                        Schedule newSche=Schedule.parseJsonObjToObj(response);
                        mOnAddedListener.OnAddedResponse(true,newSche);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnAddedListener.OnAddedResponse(false, null);
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

    public interface OnGetSchedulesListener
    {
        void OnGotScheduleResponse(List<Schedule> mytodosList);
    }

    public interface OnAddedMemoListener
    {
        void OnAddedResponse(boolean isSuccess,Schedule newTodo);
    }

}
