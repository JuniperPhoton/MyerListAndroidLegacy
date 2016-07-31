package util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.juniperphoton.jputils.LocalSettingHelper;

import java.util.Locale;

public class ConfigHelper {
    public static boolean ISOFFLINEMODE = true;
    public static boolean ISLOADLISTONCE = false;

    public static String getSid() {
        return LocalSettingHelper.getString(AppExtension.getInstance(), "sid");
    }

    public static String getAccessToken() {
        return LocalSettingHelper.getString(AppExtension.getInstance(), "access_token");
    }

    public static void configAppSetting() {
        if (!LocalSettingHelper.checkKey(AppExtension.getInstance(), "ShowKeyboard")) {
            LocalSettingHelper.putBoolean(AppExtension.getInstance(), "ShowKeyboard", true);
        }
        if (!LocalSettingHelper.checkKey(AppExtension.getInstance(), "AddToBottom")) {
            LocalSettingHelper.putBoolean(AppExtension.getInstance(), "AddToBottom", true);
        }
        if (!LocalSettingHelper.checkKey(AppExtension.getInstance(), "HandHobbit")) {
            LocalSettingHelper.putBoolean(AppExtension.getInstance(), "HandHobbit", true);
        }

        Resources resources = AppExtension.getInstance().getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        if (!LocalSettingHelper.checkKey(AppExtension.getInstance(), "Language")) {
            if (config.locale == Locale.SIMPLIFIED_CHINESE) {
                LocalSettingHelper.putString(AppExtension.getInstance(), "Language", "Chinese");
            } else LocalSettingHelper.putString(AppExtension.getInstance(), "Language", "English");
        } else {
            if (LocalSettingHelper.getString(AppExtension.getInstance(), "Language").equals("Chinese")) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
                resources.updateConfiguration(config, dm);
            } else {
                config.locale = Locale.ENGLISH;
                resources.updateConfiguration(config, dm);
            }
        }
    }
}
