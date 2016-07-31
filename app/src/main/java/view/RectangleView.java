package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.juniperphoton.myerlistandroid.R;

import util.AppExtension;

/**
 * Created by JuniperPhoton on 2016-07-31.
 */
public class RectangleView extends ColorView {

    public RectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(getColor());

        canvas.drawRect(0,0,getWidth(),getWidth(),paint);
    }
}
