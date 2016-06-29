package util;

import android.graphics.Color;

/**
 * Created by dengw on 2016-06-29.
 */
public class ColorUtil {
    public static int MakeColorDarker(int originalColor) {
        int red = Color.red(originalColor);
        int green = Color.green(originalColor);
        int blue = Color.blue(originalColor);

        return Color.argb(255, (int) (red * 0.7), (int) (green * 0.7), (int) (blue * 0.7));
    }
}
