package api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import interfaces.IRequestCallback;
import model.ToDo;
import util.ConfigHelper;
import util.NetworkSecurityHelper;

/**
 * Created by dengw on 3/4/2016.
 */
public class CloudServices {
    public static void CheckExist(final String email,final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams requestParams = new com.loopj.android.http.RequestParams();
        requestParams.add("email", email);
        client.post(UrlHelper.UserCheckExist, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                callback.onResponse(null);
            }
        });
    }

    public static void GetSalt(String email, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();
        params.put("email", email);
        client.post(UrlHelper.UserGetSalt, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                callback.onResponse(null);
            }
        });
    }

    public static void Register(final String email, final String password, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();

        String psAfterMD5 = NetworkSecurityHelper.get32MD5Str(password);
        params.put("email", email);
        params.put("password", psAfterMD5);

        client.post(UrlHelper.UserRegisterUri, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                callback.onResponse(object);
            }
        });
    }

    public static void Login(final String email, final String password,
                             final String salt, final IRequestCallback callback)
            throws NoSuchAlgorithmException {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();

        String psAfterMD5 = NetworkSecurityHelper.get32MD5Str(password);
        String psToPost = NetworkSecurityHelper.get32MD5Str(psAfterMD5 + salt);

        params.put("email", email);
        params.put("password", psToPost);

        client.post(UrlHelper.UserLoginUri, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                callback.onResponse(object);
            }
        });
    }

    public static void GetLatestSchedules(final String sid, final String access_token, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();
        params.put("sid", sid);

        client.post(UrlHelper.ScheduleGetUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                callback.onResponse(null);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);

            }
        });
    }

    public static void GetListOrder(String sid,String access_token,final IRequestCallback callback){
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();
        params.put("sid", sid);
        client.post(UrlHelper.ScheduleGetOrderUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                callback.onResponse(null);
            }
        });
    }

    public static void AddToDo(String sid, String content,final String access_token, String isDone,
                               int cate, final IRequestCallback callback) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(2000);
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();
        params.put("sid", sid);
        params.put("time", sdf.format(date));
        params.put("content", content);
        params.put("isdone", isDone);
        params.put("cate", String.valueOf(cate));
        client.post(UrlHelper.ScheduleAddUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callback.onResponse(null);
            }
        });

    }

    public static void SetListOrder(String sid,String access_token, String order,final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();
        params.put("sid", sid);
        params.put("order", order);
        client.post(UrlHelper.ScheduleSetOrderUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callback.onResponse(null);
            }
        });
    }

    public static void SetDone(String sid,String access_token, String id, String isDone,final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("isdone", isDone);
        client.post(UrlHelper.ScheduleFinishUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callback.onResponse(null);
            }

        });
    }

    public static void SetDelete(String sid,String access_token, String id,final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();
        params.put("id", id);
        client.post(UrlHelper.ScheduleDeleteUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callback.onResponse(null);
            }
        });
    }

    public static void UpdateContent(String sid,String access_token, String id, String content, int cate,final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        com.loopj.android.http.RequestParams params = new com.loopj.android.http.RequestParams();
        params.put("id", id);
        params.put("content", content);
        params.put("cate", String.valueOf(cate));
        client.post(UrlHelper.ScheduleUpdateUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callback.onResponse(null);
            }
        });
    }
}
