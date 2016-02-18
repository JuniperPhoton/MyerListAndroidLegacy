package api;

import android.widget.Toast;

import org.apache.http.client.methods.HttpPut;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.request.HttpRequest;
import org.xutils.x;

import java.net.URL;

/**
 * Created by juniperphoton on 16/2/18.
 */
public class APIUtil {
    public void sendGetRequest(URL url) {
        try {
            RequestParams params = new RequestParams("https://www.baidu.com/s");
            params.addQueryStringParameter("wd", "xUtils");

            x.http().get(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {

                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFinished() {

                }
            });
        }
        catch (Exception e) {

        }

    }
}
