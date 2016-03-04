package api;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by juniperphoton on 16/2/18.
 */

public class APIUtil {
    OkHttpClient client = new OkHttpClient();
    public static StringResponse sendGetRequest(String url){
        RequestBody body = RequestBody.create()
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static StringResponse requestString(HttpRequest.HttpMethod method, String url, RequestParams params) {

        ResponseStream rs = null;

        int statusCode;

        try {
            rs = new HttpUtils().sendSync(method, url, params);
        } catch (HttpException e) {
            e.printStackTrace();
            return new StringResponse(HttpStatus.SC_EXPECTATION_FAILED, null);
        }

        statusCode = rs.getStatusCode();

        String response = null;

        try {
            response = rs.readString();
        } catch (IOException e) {
            e.printStackTrace();
            statusCode = HttpStatus.SC_EXPECTATION_FAILED;
        }

        return new StringResponse(statusCode, response);
    }

    public static Response request(HttpRequest.HttpMethod method, String url, RequestParams params) {
        StringResponse stringResponse = requestString(method, url, params);

        if (stringResponse == null) {
            return new Response(HttpStatus.SC_EXPECTATION_FAILED);
        } else if (!stringResponse.isSuccess()) {
            return new Response(stringResponse.getStatusCode());
        }

        Response response = new Response();

        try {
            JSONObject responseJson = new JSONObject(stringResponse.getResponse());
            response.setErrorCode(responseJson.getInt("errorCode"));
            response.setErrorMsg(responseJson.getString("errorMsg"));

            try {
                response.setData(responseJson.getString("datas"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            response = null;
            e.printStackTrace();
            return new Response(HttpStatus.SC_EXPECTATION_FAILED);
        }

        return response;
    }
}
