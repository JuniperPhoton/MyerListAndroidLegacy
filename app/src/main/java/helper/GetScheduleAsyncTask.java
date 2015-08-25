package helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import model.Schedule;
import okio.BufferedSink;

/**
 * Created by dengw on 7/26/2015.
 */
public class GetScheduleAsyncTask extends AsyncTask<Void,Void,ArrayList<Schedule>>
{
    private Context mContext;

    public GetScheduleAsyncTask(Context context)
    {
        mContext=context;
    }

    @Override
    protected ArrayList doInBackground(Void... params)
    {
        try
        {
            String url=PostHelper.ScheduleGetUri + "sid=" + ConfigHelper.getString(mContext, "sid") + "&access_token=" + ConfigHelper.getString(mContext, "access_token");
            OkHttpClient client=new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url).post(new RequestBody()
                    {
                        @Override
                        public MediaType contentType()
                        {
                            return null;
                        }

                        @Override
                        public void writeTo(BufferedSink sink) throws IOException
                        {

                        }
                    }).build();
            Response response = client.newCall(request).execute();

            HttpResponse response = client.execute(post);
            String content = EntityUtils.toString(response.getEntity());
            JSONObject responseJSON=new JSONObject(content);
            JSONArray array=responseJSON.getJSONArray("ScheduleInfo");
            final ArrayList<Schedule> todosList=Schedule.parseJsonObjFromArray(array);

            post=new HttpPost(PostHelper.ScheduleGetOrderUri+"sid="+ConfigHelper.getString(mContext,"sid")+"&access_token="+ConfigHelper.getString(mContext,"access_token"));
            response = client.execute(post);
            content = EntityUtils.toString(response.getEntity());
            responseJSON=new JSONObject(content);

            boolean isSuccess=responseJSON.getBoolean("isSuccessed");
            if(isSuccess)
            {
                String orderStr=responseJSON.getJSONArray(("OrderList")).getJSONObject(0).getString("list_order");
                ArrayList<Schedule> listToReturn=Schedule.setOrderByString(todosList,orderStr);

                Log.d("TAG",array.toString());
                return listToReturn;
            }

            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new ArrayList<Schedule>();
    }
}
