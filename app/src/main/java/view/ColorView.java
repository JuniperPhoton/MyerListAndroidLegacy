package view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.juniperphoton.myerlistandroid.R;

import common.App;

/**
 * Created by JuniperPhoton on 2016-07-31.
 */
public class ColorView extends View {
    private int mcolor;

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mcolor = ContextCompat.getColor(App.getInstance(), R.color.MyerListBlue);
    }

    public int getColor() {
        return mcolor;
    }

    public void setColor(int color) {
        mcolor = color;
        invalidate();
    }
}
