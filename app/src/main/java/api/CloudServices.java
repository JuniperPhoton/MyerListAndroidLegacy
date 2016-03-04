package api;

import android.content.Context;

import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import interfaces.IRequestCallbacks;

/**
 * Created by dengw on 3/4/2016.
 */
public class CloudServices {

    public static boolean CheckExist(String email) {
        RequestParams params = new RequestParams();
        params.addBodyParameter("email", email);
        StringResponse stringResponse = APIUtil.requestString(HttpRequest.HttpMethod.POST, UrlHelper.UserCheckExist, params);
        if (!stringResponse.isSuccess()) {
            return false;
        }
        if (stringResponse.getResponse() == null) {
            return false;
        }
        String response = stringResponse.getResponse();
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean isOK = jsonObject.getBoolean("isSuccessed");
            if (!isOK) {
                return false;
            }
            boolean isExist = jsonObject.getBoolean("isExist");
            if (!isExist) {
                return false;
            }
            return true;
        }
        catch (JSONException e) {
            return false;
        }
    }
}
