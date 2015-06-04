package helper;

import android.content.Context;
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
}
