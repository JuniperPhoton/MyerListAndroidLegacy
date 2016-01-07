package util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juniper.myerlistandroid.R;

/**
 * Created by juniperphoton on 6/5/2015.
 */
public class AppUtil
{
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivityManager)
        {
            NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();

            if (null != networkInfo)
            {
                for (NetworkInfo info : networkInfo)
                {
                    if (info.getState() == NetworkInfo.State.CONNECTED)
                    {
                        // Log.e(TAG, "the net is ok");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
