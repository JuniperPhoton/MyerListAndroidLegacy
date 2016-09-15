package util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.juniperphoton.jputils.LocalSettingHelper;

import java.util.Locale;

import common.App;

public class AppConfig {
    public static boolean ISOFFLINEMODE = true;
    public static boolean ISLOADLISTONCE = false;

    public static String getSid() {
        return LocalSettingHelper.getString(App.getInstance(), "sid");
    }

    public static String getAccessToken() {
        return LocalSettingHelper.getString(App.getInstance(), "access_token");
    }

    public static boolean canSync(){
        return !AppConfig.ISOFFLINEMODE && AppUtil.isNetworkAvailable(App.getInstance());
    }

    public static void configAppSetting() {
        if (!LocalSettingHelper.checkKey(App.getInstance(), "ShowKeyboard")) {
            LocalSettingHelper.putBoolean(App.getInstance(), "ShowKeyboard", true);
        }
        if (!LocalSettingHelper.checkKey(App.getInstance(), "AddToBottom")) {
            LocalSettingHelper.putBoolean(App.getInstance(), "AddToBottom", true);
        }
        if (!LocalSettingHelper.checkKey(App.getInstance(), "HandHobbit")) {
            LocalSettingHelper.putBoolean(App.getInstance(), "HandHobbit", true);
        }

        Resources resources = App.getInstance().getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        if (!LocalSettingHelper.checkKey(App.getInstance(), "Language")) {
            if (config.locale == Locale.SIMPLIFIED_CHINESE) {
                LocalSettingHelper.putString(App.getInstance(), "Language", "Chinese");
            } else LocalSettingHelper.putString(App.getInstance(), "Language", "English");
        } else {
            if (LocalSettingHelper.getString(App.getInstance(), "Language").equals("Chinese")) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
                resources.updateConfiguration(config, dm);
            } else {
                config.locale = Locale.ENGLISH;
                resources.updateConfiguration(config, dm);
            }
        }
    }
}
