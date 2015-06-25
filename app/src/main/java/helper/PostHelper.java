package helper;

import android.content.Context;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;

import model.Schedule;

public class PostHelper
{


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
    public static OnCheckResponseCallback mOnCheckResponseCallback;
    public static OnGetSaltResponseCallback mOnGetSaltResponseCallback;
    public static OnLoginResponseCallback mOnLoginResponseCallback;
    public static OnGetSchedulesCallback mOnGetSchedulesCallback;
    public static OnAddedMemoCallback mOnAddedCallback;
    public static OnSetOrderCallback mOnSetOrderCallback;
    public static OnRegisterCallback mOnRegisteredListener;
    public static OnDoneCallback mOnDoneCallback;
    public static OnDeleteCallback mOnDeleteCallback;
    public static OnUpdateContentCallback mOnUpdateCallback;


    public  static void CheckExist(Context context,final String email)
    {
        mOnCheckResponseCallback =(OnCheckResponseCallback)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams requestParams=new RequestParams();
        requestParams.add("email", email);
        client.post(UserCheckExist, requestParams,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try
                {
                    boolean isSuccess=response.getBoolean("isSuccessed");
                    if(isSuccess)
                    {
                        boolean isExist=response.getBoolean("isExist");
                        if(isExist)
                        {
                            mOnCheckResponseCallback.OnCheckResponse(true);
                        }
                        else mOnCheckResponseCallback.OnCheckResponse(false);
                    }
                    else mOnCheckResponseCallback.OnCheckResponse(false);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnCheckResponseCallback.OnCheckResponse(false);
                }
            }

        });
    }

    public static void GetSalt(Context context,String email)
    {
        mOnGetSaltResponseCallback =(OnGetSaltResponseCallback)context;
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("email", email);
        client.post(UserGetSalt,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try
                {
                    boolean isSuccess=response.getBoolean("isSuccessed");
                    if(isSuccess)
                    {
                        String salt=response.getString("Salt");

                        mOnGetSaltResponseCallback.OnGetSaltResponse(salt);
                    }
                    else mOnGetSaltResponseCallback.OnGetSaltResponse(null);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    try
                    {
                        mOnGetSaltResponseCallback.OnGetSaltResponse(null);
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

    public static void Register(Context context,final String email, final String password)
    {
        mOnRegisteredListener=(OnRegisterCallback)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();

        String psAfterMD5=NetworkSecurityHelper.get32MD5Str(password);
        params.put("email",email);
        params.put("password",psAfterMD5);

        client.post(UserRegisterUri,params,new JsonHttpResponseHandler()
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
                            String salt = userObj.getString("Salt");
                            ConfigHelper.putString(ContextUtil.getInstance(), "email", email);
                            ConfigHelper.putString(ContextUtil.getInstance(), "password", password);
                            ConfigHelper.putString(ContextUtil.getInstance(),"salt",salt);

                            mOnRegisteredListener.OnRegisteredResponse(true,salt);
                        } else mOnRegisteredListener.OnRegisteredResponse(false, null);
                    } else mOnRegisteredListener.OnRegisteredResponse(false, null);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnRegisteredListener.OnRegisteredResponse(false, null);
                }
            }
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object)
            {
                mOnRegisteredListener.OnRegisteredResponse(false, null);
            }
        });
    }

    public static void Login(Context context, final String email, final String password, final String salt) throws NoSuchAlgorithmException
    {
        mOnLoginResponseCallback =(OnLoginResponseCallback)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();

        String psAfterMD5=NetworkSecurityHelper.get32MD5Str(password);
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
                            ConfigHelper.DeleteKey(ContextUtil.getInstance(), "password");
                            ConfigHelper.putString(ContextUtil.getInstance(),"salt",salt);
                            ConfigHelper.putString(ContextUtil.getInstance(), "sid", sid);
                            ConfigHelper.putString(ContextUtil.getInstance(), "access_token", access_token);
                            mOnLoginResponseCallback.OnLoginResponse(true);
                        } else mOnLoginResponseCallback.OnLoginResponse(false);
                    } else mOnLoginResponseCallback.OnLoginResponse((false));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnLoginResponseCallback.OnLoginResponse((false));
                }

            }
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object)
            {
                mOnLoginResponseCallback.OnLoginResponse((false));
            }

        });
    }

    public static void GetOrderedSchedules(Context context,final String sid, final String access_token)
    {
        mOnGetSchedulesCallback =(OnGetSchedulesCallback)context;

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
                            final ArrayList<Schedule> todosList=Schedule.parseJsonObjFromArray(array);

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
                                            ArrayList<Schedule> listToReturn=Schedule.setOrderByString(todosList,orderStr);
                                            mOnGetSchedulesCallback.OnGotScheduleResponse(listToReturn);
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
        mOnAddedCallback =(OnAddedMemoCallback)context;

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
                        mOnAddedCallback.OnAddedResponse(true, newSche);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnAddedCallback.OnAddedResponse(false, null);
                }

            }

        });
    }

    public static void SetListOrder(Context context,String sid,String order)
    {
        mOnSetOrderCallback =(OnSetOrderCallback)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("sid",sid);
        params.put("order", order);
        client.post(ScheduleSetOrderUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context,"access_token"), params, new JsonHttpResponseHandler()
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
                        mOnSetOrderCallback.OnSetOrderResponse(true);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnSetOrderCallback.OnSetOrderResponse(false);
                }

            }

        });
    }

    public static void SetDone(Context context,String sid,String id,String isDone)
    {
        mOnDoneCallback =(OnDoneCallback)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("id",id);
        params.put("isdone", isDone);
        client.post(ScheduleFinishUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context,"access_token"), params, new JsonHttpResponseHandler()
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
                        mOnDoneCallback.OnDoneResponse(true);
                    }
                    else
                    {
                        mOnDoneCallback.OnDoneResponse(false);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnDoneCallback.OnDoneResponse(false);
                }

            }

        });
    }

    public static void SetDelete(Context context,String sid,String id)
    {
        mOnDeleteCallback =(OnDeleteCallback)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("id",id);
        client.post(ScheduleDeleteUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context,"access_token"), params, new JsonHttpResponseHandler()
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
                        mOnDeleteCallback.OnDeleteResponse(true);
                    }
                    else
                    {
                        mOnDeleteCallback.OnDeleteResponse(false);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnDeleteCallback.OnDeleteResponse(false);
                }

            }

        });
    }

    public static void UpdateContent(Context context,String sid,String id,String content)
    {
        mOnUpdateCallback =(OnUpdateContentCallback)context;

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("id",id);
        params.put("content",content);
        client.post(ScheduleUpdateUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context,"access_token"), params, new JsonHttpResponseHandler()
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
                        mOnUpdateCallback.OnUpdateContent(true);
                    }
                    else
                    {
                        mOnUpdateCallback.OnUpdateContent(false);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mOnUpdateCallback.OnUpdateContent(false);
                }

            }

        });
    }

    public interface OnCheckResponseCallback
    {
        void OnCheckResponse(boolean check);
    }

    public interface OnGetSaltResponseCallback
    {
        void OnGetSaltResponse(String str) throws NoSuchAlgorithmException;
    }

    public interface OnLoginResponseCallback
    {
        void OnLoginResponse(boolean value);
    }

    public interface OnGetSchedulesCallback
    {
        void OnGotScheduleResponse(ArrayList<Schedule> mytodosList);
    }

    public interface OnAddedMemoCallback
    {
        void OnAddedResponse(boolean isSuccess,Schedule newTodo);
    }

    public interface OnSetOrderCallback
    {
        void OnSetOrderResponse(boolean isSuccess);
    }

    public interface OnRegisterCallback
    {
        void OnRegisteredResponse(boolean isSuccess,String salt);
    }

    public interface OnDoneCallback
    {
        void OnDoneResponse(boolean isSuccess);
    }

    public interface OnDeleteCallback
    {
        void OnDeleteResponse(boolean isSuccess);
    }

    public interface OnUpdateContentCallback
    {
        void OnUpdateContent(boolean isSuccess);
    }
}
