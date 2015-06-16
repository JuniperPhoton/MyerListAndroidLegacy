package helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import java.util.Locale;

public  class ConfigHelper
{
    private static String name="config";
    public static boolean ISOFFLINEMODE=true;

    public  static  void ConfigAppSetting()
    {
        if(!checkKey(ContextUtil.getInstance(),"ShowKeyboard"))
        {
            putBoolean(ContextUtil.getInstance(),"ShowKeyboard",true);
        }
        if(!checkKey(ContextUtil.getInstance(),"AddToBottom"))
        {
            putBoolean(ContextUtil.getInstance(),"AddToBottom",true);
        }
        if(!checkKey(ContextUtil.getInstance(),"HandHobbit"))
        {
            putBoolean(ContextUtil.getInstance(),"HandHobbit",true);
        }

        Resources resources = ContextUtil.getInstance().getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources .getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。

        if(!checkKey(ContextUtil.getInstance(),"Language"))
        {
            if(config.locale==Locale.SIMPLIFIED_CHINESE)
            {
                putString(ContextUtil.getInstance(), "Language", "Chinese");
            }
            else putString(ContextUtil.getInstance(),"Language","English");
        }
        else
        {
            if(ConfigHelper.getString(ContextUtil.getInstance(), "Language").equals("Chinese"))
            {
                config.locale=Locale.SIMPLIFIED_CHINESE;
                resources.updateConfiguration(config,dm);
            }
            else
            {
                config.locale=Locale.ENGLISH;
                resources.updateConfiguration(config,dm);
            }
        }
    }

    public static SharedPreferences getSharedPreference(Context context)
    {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(Context context,String key)
    {
        SharedPreferences sharedPreferences= ConfigHelper.getSharedPreference(context);
        return sharedPreferences.getBoolean(key, false);
    }

    public  static boolean checkKey(Context context,String key)
    {
        SharedPreferences sharedPreferences= ConfigHelper.getSharedPreference(context);
        return sharedPreferences.contains(key);
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
