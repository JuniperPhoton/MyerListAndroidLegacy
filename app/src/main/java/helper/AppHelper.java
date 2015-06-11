package helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by juniperphoton on 6/5/2015.
 */
public class AppHelper
{
    public static void ShowShortToast(String str)
    {
        Toast.makeText(ContextUtil.getInstance(),str,Toast.LENGTH_SHORT).show();
    }
    public static void ShowLongToast(String str)
    {
        Toast.makeText(ContextUtil.getInstance(),str,Toast.LENGTH_LONG).show();
    }

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivityManager)
        {
            NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();

            if (null != networkInfo) {
                for (NetworkInfo info : networkInfo) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        // Log.e(TAG, "the net is ok");
                        return true;
                    }
                }
            }
        }
        Toast.makeText(context, "Please check your network ;-)", Toast.LENGTH_SHORT).show();
        return false;
    }
}
