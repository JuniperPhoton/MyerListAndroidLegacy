package com.juniperphoton.jputils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.jar.Attributes;

/**
 * Created by JuniperPhoton on 2016-07-31.
 */
public class LocalSettingHelper {
    private static final String CONFIG_NAME = "config";

    public static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreference(context);
        return sharedPreferences.getBoolean(key, false);
    }

    public static boolean checkKey(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreference(context);
        return sharedPreferences.contains(key);
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreference(context);
        return sharedPreferences.getString(key, null);
    }

    public static boolean putString(Context context, String key, String value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static boolean putBoolean(Context context, String key, Boolean value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean putInt(Context context, String key, int value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static boolean deleteKey(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        return editor.commit();
    }
}
