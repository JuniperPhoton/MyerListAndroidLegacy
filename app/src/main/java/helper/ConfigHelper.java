package helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

/**
 * Created by juniperphoton on 6/3/2015.
 */
public  class ConfigHelper
{
    private static String name="config";
    public static boolean ISOFFLINEMODE=true;

    public static SharedPreferences getSharedPreference(Context context)
    {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(Context context,String key)
    {
        SharedPreferences sharedPreferences= ConfigHelper.getSharedPreference(context);
        return sharedPreferences.getBoolean(key, false);
    }

    public static String getString(Context context,String key)
    {
        SharedPreferences sharedPreferences=ConfigHelper.getSharedPreference(context);
        return sharedPreferences.getString(key, null);
    }

    public static boolean putString(Context context, String key, String value)
    {
        SharedPreferences sharedPreference = ConfigHelper.getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static boolean putBoolean(Context context, String key, Boolean value)
    {
        SharedPreferences sharedPreference = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean putInt(Context context, String key, int value)
    {
        SharedPreferences sharedPreference = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static boolean DeleteKey(Context context, String key)
    {
        SharedPreferences sharedPreferences=getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        return editor.commit();
    }

}
