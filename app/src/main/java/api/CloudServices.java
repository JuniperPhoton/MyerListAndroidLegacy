package api;

import android.util.Log;

import com.juniperphoton.jputils.NetworkSecurityHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import interfaces.IRequestCallback;

/**
 * Created by dengw on 3/4/2016.
 */
public class CloudServices {
    private static String TAG=CloudServices.class.getName();

    public static void checkExist(final String email, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
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

    public static void getSalt(String email, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
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

    public static void register(final String email, final String password, final IRequestCallback callback) {
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

    public static void login(final String email, final String password,
                             final String salt, final IRequestCallback callback)
            throws NoSuchAlgorithmException {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

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

    public static void getLatestSchedules(final String sid, final String access_token, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(2000);
        RequestParams params = new RequestParams();
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

    public static void getListOrder(String sid, String access_token, final IRequestCallback callback) {
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

    public static void addToDo(String sid,  final String access_token, String content,String isDone,
                               int cate, final IRequestCallback callback) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(10000);
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

    public static void setListOrder(String sid, String access_token, String order, final IRequestCallback callback) {
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

    public static void setDone(String sid, String access_token, String id, String isDone, final IRequestCallback callback) {
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

    public static void setDelete(String sid, String access_token, String id, final IRequestCallback callback) {
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

    public static void updateContent(String sid, String access_token, String id, String content, int cate, final IRequestCallback callback) {
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

    public static void getCates(String sid, String access_token, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        client.setTimeout(10000);
        client.get(UrlHelper.UserGetCateUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onResponse(response);
                Log.d(TAG,response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callback.onResponse(null);
                Log.d(TAG,errorResponse.toString());
            }
        });
    }

    public static void updateCates(String sid, String access_token, String content, final IRequestCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("cate_info",content);
        client.post(UrlHelper.UserUpdateCateUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
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
