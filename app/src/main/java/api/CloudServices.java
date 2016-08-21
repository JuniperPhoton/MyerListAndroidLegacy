package api;

import com.juniperphoton.jputils.NetworkSecurityHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import interfaces.IRequestCallback;

public class CloudServices {

    private static String TAG = CloudServices.class.getName();

    private static AsyncHttpClient mClient = new AsyncHttpClient();

    public static void checkExist(final String email, final IRequestCallback callback) {
        RequestParams requestParams = new RequestParams();
        requestParams.add("email", email);

        Logger.d("checkexist");

        mClient.post(UrlHelper.UserCheckExist, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

    public static void getSalt(String email, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.put("email", email);

        Logger.d("getSalt");

        mClient.post(UrlHelper.UserGetSalt, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

    public static void register(final String email, final String password, final IRequestCallback callback) {
        RequestParams params = new RequestParams();

        String psAfterMD5 = NetworkSecurityHelper.get32MD5Str(password);
        params.put("email", email);
        params.put("password", psAfterMD5);

        Logger.d("register");

        mClient.post(UrlHelper.UserRegisterUri, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                if (callback != null) {
                    callback.onResponse(object);
                }
            }
        });
    }

    public static void login(final String email, final String passwordAfterMD5, final IRequestCallback callback) {
        RequestParams params = new RequestParams();

        Logger.d("login");

        params.put("email", email);
        params.put("password", passwordAfterMD5);

        mClient.post(UrlHelper.UserLoginUri, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                if (callback != null) {
                    callback.onResponse(object);
                }
            }
        });
    }

    public static void getLatestSchedules(final String sid, final String access_token, boolean isSync, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.put("sid", sid);

        Logger.d("getLatestSchedules");
        AsyncHttpClient client = isSync ? new SyncHttpClient() : mClient;
        client.post(UrlHelper.ScheduleGetUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }
        });
    }

    public static void getListOrder(String sid, String access_token, boolean isSync, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.put("sid", sid);

        Logger.d("getListOrder");

        AsyncHttpClient client = isSync ? new SyncHttpClient() : mClient;
        client.post(UrlHelper.ScheduleGetOrderUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int code, Header[] headers, Throwable throwable, JSONObject object) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

    public static void addToDo(String sid, final String access_token, String content, String isDone,
                               int cate, final IRequestCallback callback) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        Logger.d("addToDo");

        RequestParams params = new RequestParams();
        params.put("sid", sid);
        params.put("time", sdf.format(date));
        params.put("content", content);
        params.put("isdone", isDone);
        params.put("cate", String.valueOf(cate));
        mClient.post(UrlHelper.ScheduleAddUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });

    }

    public static void setListOrder(String sid, String access_token, String order, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.put("sid", sid);
        params.put("order", order);

        Logger.d("setListOrder");

        mClient.post(UrlHelper.ScheduleSetOrderUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

    public static void setDone(String sid, String access_token, String id, String isDone, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("isdone", isDone);

        Logger.d("setDone");

        mClient.post(UrlHelper.ScheduleFinishUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }

        });
    }

    public static void setDelete(String sid, String access_token, String id, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.put("id", id);

        Logger.d("setDelete");

        mClient.post(UrlHelper.ScheduleDeleteUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

    public static void updateContent(String sid, String access_token, String id, String content, int cate, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("content", content);
        params.put("cate", String.valueOf(cate));

        Logger.d("updateContent");

        mClient.post(UrlHelper.ScheduleUpdateUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

    public static void getCategories(String sid, String access_token, boolean isSync, final IRequestCallback callback) {
        RequestParams params = new RequestParams();

        Logger.d("getCategories");

        AsyncHttpClient client = isSync ? new SyncHttpClient() : mClient;
        client.get(UrlHelper.UserGetCateUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                if (callback != null) {
                    callback.onResponse(jsonObject);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

    public static void updateCategories(String sid, String access_token, String content, final IRequestCallback callback) {
        RequestParams params = new RequestParams();
        params.add("cate_info", content);

        Logger.d("updateCategories");

        mClient.post(UrlHelper.UserUpdateCateUri + "sid=" + sid + "&access_token=" + access_token, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }
}
