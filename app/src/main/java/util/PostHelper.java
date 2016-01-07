package util;

import android.content.Context;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import interfaces.IRequestCallbacks;
import model.ToDo;

public class PostHelper
{

    public final static String domain = "juniperphoton.net";
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

    private static boolean mExist = false;
    //    public static OnCheckResponseCallback mOnCheckResponseCallback;
//    public static OnGetSaltResponseCallback mOnGetSaltResponseCallback;
//    public static OnLoginResponseCallback mOnLoginResponseCallback;
//    public static OnGetSchedulesCallback mOnGetSchedulesCallback;
//    public static OnAddedMemoCallback mOnAddedCallback;
//    public static OnSetOrderCallback mOnSetOrderCallback;
//    public static OnRegisterCallback mOnRegisteredListener;
//    public static OnDoneCallback mOnDoneCallback;
//    public static OnDeleteCallback mOnDeleteCallback;
//    public static OnUpdateContentCallback mOnUpdateCallback;
    public static IRequestCallbacks mRequestCallback;

    public static void CheckExist(Context context, final String email)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        requestParams.add("email", email);
        client.post(UserCheckExist, requestParams, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                // If the response is JSONObject instead of expected JSONArray
                try
                {
                    boolean isSuccess = response.getBoolean("isSuccessed");
                    if (isSuccess)
                    {
                        boolean isExist = response.getBoolean("isExist");
                        if (isExist)
                        {
                            mRequestCallback.OnCheckResponse(true);
                        }
                        else mRequestCallback.OnCheckResponse(false);
                    }
                    else mRequestCallback.OnCheckResponse(false);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnCheckResponse(false);
                }
            }

        });
    }

    public static void GetSalt(Context context, String email)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        client.post(UserGetSalt, params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                try
                {
                    boolean isSuccess = response.getBoolean("isSuccessed");
                    if (isSuccess)
                    {
                        String salt = response.getString("Salt");

                        mRequestCallback.OnGetSaltResponse(salt);
                    }
                    else mRequestCallback.OnGetSaltResponse(null);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void Register(Context context, final String email, final String password)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        String psAfterMD5 = NetworkSecurityHelper.get32MD5Str(password);
        params.put("email", email);
        params.put("password", psAfterMD5);

        client.post(UserRegisterUri, params, new JsonHttpResponseHandler()
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
                            ConfigHelper.putString(ContextUtil.getInstance(), "salt", salt);

                            mRequestCallback.OnRegisteredResponse(true, salt);
                        }
                        else mRequestCallback.OnRegisteredResponse(false, null);
                    }
                    else mRequestCallback.OnRegisteredResponse(false, null);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnRegisteredResponse(false, null);
                }
            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object)
            {
                mRequestCallback.OnRegisteredResponse(false, null);
            }
        });
    }

    public static void Login(Context context, final String email, final String password, final String salt) throws NoSuchAlgorithmException
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        String psAfterMD5 = NetworkSecurityHelper.get32MD5Str(password);
        String psToPost = NetworkSecurityHelper.get32MD5Str(psAfterMD5 + salt);

        params.put("email", email);
        params.put("password", psToPost);

        client.post(UserLoginUri, params, new JsonHttpResponseHandler()
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
                            ConfigHelper.putString(ContextUtil.getInstance(), "salt", salt);
                            ConfigHelper.putString(ContextUtil.getInstance(), "sid", sid);
                            ConfigHelper.putString(ContextUtil.getInstance(), "access_token", access_token);
                            mRequestCallback.OnLoginResponse(true);
                        }
                        else mRequestCallback.OnLoginResponse(false);
                    }
                    else mRequestCallback.OnLoginResponse((false));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnLoginResponse((false));
                }

            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object)
            {
                mRequestCallback.OnLoginResponse((false));
            }

        });
    }

    public static void GetOrderedSchedules(Context context, final String sid, final String access_token)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("sid", sid);

        client.post(ScheduleGetUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler()
        {
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object)
            {
                mRequestCallback.OnGotScheduleResponse(false, null);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                Boolean isSuccess = null;
                try
                {
                    isSuccess = response.getBoolean("isSuccessed");
                    if (isSuccess)
                    {
                        JSONArray array = response.getJSONArray("ScheduleInfo");

                        if (array != null)
                        {
                            final ArrayList<ToDo> todosList = ToDo.parseJsonObjFromArray(array);

                            AsyncHttpClient client = new AsyncHttpClient();
                            RequestParams params = new RequestParams();
                            params.put("sid", sid);
                            client.post(ScheduleGetOrderUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler()
                            {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                                {
                                    try
                                    {
                                        boolean isSuccess = response.getBoolean("isSuccessed");
                                        if (isSuccess)
                                        {
                                            String orderStr = response.getJSONArray(("OrderList")).getJSONObject(0).getString("list_order");
                                            ArrayList<ToDo> listToReturn = ToDo.setOrderByString(todosList, orderStr);
                                            mRequestCallback.OnGotScheduleResponse(true, listToReturn);
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        mRequestCallback.OnGotScheduleResponse(false, null);
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    }

                }
                catch (Exception e)
                {
                    mRequestCallback.OnGotScheduleResponse(false, null);
                    e.printStackTrace();
                }

            }

        });
    }

    public static void AddToDo(Context context, String sid, String content, String isDone, int cate)
    {
        mRequestCallback = (IRequestCallbacks) context;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(2000);
        RequestParams params = new RequestParams();
        params.put("sid", sid);
        params.put("time", sdf.format(date));
        params.put("content", content);
        params.put("isdone", isDone);
        params.put("cate", String.valueOf(cate));
        client.post(ScheduleAddUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context, "access_token"), params, new JsonHttpResponseHandler()
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
                        ToDo newToDo = ToDo.parseJsonObjToObj(response.getJSONObject("ScheduleInfo"));
                        mRequestCallback.OnAddedResponse(true, newToDo);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnAddedResponse(false, null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
            {
                mRequestCallback.OnAddedResponse(false, null);
            }
        });

    }

    public static void SetListOrder(Context context, String sid, String order)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("sid", sid);
        params.put("order", order);
        client.post(ScheduleSetOrderUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context, "access_token"), params, new JsonHttpResponseHandler()
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
                        ToDo newSche = ToDo.parseJsonObjToObj(response);
                        mRequestCallback.OnSetOrderResponse(true);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnSetOrderResponse(false);
                }

            }

        });
    }

    public static void SetDone(Context context, String sid, String id, String isDone)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("isdone", isDone);
        client.post(ScheduleFinishUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context, "access_token"), params, new JsonHttpResponseHandler()
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
                        mRequestCallback.OnDoneResponse(true);
                    }
                    else
                    {
                        mRequestCallback.OnDoneResponse(false);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnDoneResponse(false);
                }

            }

        });
    }

    public static void SetDelete(Context context, String sid, String id)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);
        client.post(ScheduleDeleteUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context, "access_token"), params, new JsonHttpResponseHandler()
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
                        mRequestCallback.OnDeleteResponse(true);
                    }
                    else
                    {
                        mRequestCallback.OnDeleteResponse(false);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnDeleteResponse(false);
                }

            }

        });
    }

    public static void UpdateContent(Context context, String sid, String id, String content, int cate)
    {
        mRequestCallback = (IRequestCallbacks) context;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("content", content);
        params.put("cate", String.valueOf(cate));
        client.post(ScheduleUpdateUri + "sid=" + sid + "&access_token=" + ConfigHelper.getString(context, "access_token"), params, new JsonHttpResponseHandler()
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
                        mRequestCallback.OnUpdateContent(true);
                    }
                    else
                    {
                        mRequestCallback.OnUpdateContent(false);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    mRequestCallback.OnUpdateContent(false);
                }

            }

        });
    }

//    public interface OnCheckResponseCallback
//    {
//        void OnCheckResponse(boolean check);
//    }
//
//    public interface OnGetSaltResponseCallback
//    {
//        void OnGetSaltResponse(String str) throws NoSuchAlgorithmException;
//    }
//
//    public interface OnLoginResponseCallback
//    {
//        void OnLoginResponse(boolean value);
//    }
//
//    public interface OnGetSchedulesCallback
//    {
//        void OnGotScheduleResponse(ArrayList<ToDo> mytodosList);
//    }
//
//    public interface OnAddedMemoCallback
//    {
//        void OnAddedResponse(boolean isSuccess, ToDo newTodo);
//    }
//
//    public interface OnSetOrderCallback
//    {
//        void OnSetOrderResponse(boolean isSuccess);
//    }
//
//    public interface OnRegisterCallback
//    {
//        void OnRegisteredResponse(boolean isSuccess,String salt);
//    }
//
//    public interface OnDoneCallback
//    {
//        void OnDoneResponse(boolean isSuccess);
//    }
//
//    public interface OnDeleteCallback
//    {
//        void OnDeleteResponse(boolean isSuccess);
//    }
//
//    public interface OnUpdateContentCallback
//    {
//        void OnUpdateContent(boolean isSuccess);
//    }
}
